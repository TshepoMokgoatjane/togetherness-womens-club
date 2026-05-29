package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;
import za.co.twc.togetherness.womens.club.exception.MemberMissedLastThreeConsecutiveMonthsException;
import za.co.twc.togetherness.womens.club.service.BurialClaimService;
import za.co.twc.togetherness.womens.club.service.DocumentService;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
public class BurialClaimController {

    private static final Logger LOGGER = LoggerFactory.getLogger(BurialClaimController.class);

    private final BurialClaimService burialClaimService;
    private final MemberService memberService;
    private final DocumentService documentService;

    public BurialClaimController(BurialClaimService burialClaimService, MemberService memberService, DocumentService documentService) {
        this.burialClaimService = burialClaimService;
        this.memberService = memberService;
        this.documentService = documentService;
    }

    // ==================
    // ALL CLAIMS
    // ==================
    @GetMapping("/claims")
    public String listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "claimDate") String sortBy,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) ClaimStatus status,
            Model model) {

        Page<BurialClaim> claimsData = burialClaimService.searchAndFilterClaims(page, size, sortBy, search, status);

        model.addAttribute("pageTitle", "All Burial Claims");

        model.addAttribute("claims", claimsData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", claimsData.getTotalPages());
        model.addAttribute("totalItems", claimsData.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);

        model.addAttribute("search", search);
        model.addAttribute("status", status);

        // Load documents for each claim
        java.util.Map<Long, java.util.List<za.co.twc.togetherness.womens.club.domain.Document>> claimDocuments = new java.util.HashMap<>();
        for (BurialClaim claim : claimsData.getContent()) {
            claimDocuments.put(claim.getId(), documentService.getDocumentsForClaim(claim.getId()));
        }
        model.addAttribute("claimDocuments", claimDocuments);

        return "claim/list";
    }

    // ==================
    // SUBMIT CLAIM (standalone - select member)
    // ==================
    @GetMapping("/claims/new")
    public String newClaimForm(Model model) {
        model.addAttribute("pageTitle", "Submit Burial Claim");
        model.addAttribute("members", memberService.getAllActiveMembers());
        model.addAttribute("claim", new BurialClaim());
        return "claim/new";
    }

    @PostMapping("/claims")
    public String createClaimStandalone(@RequestParam(required = false) Long memberId,
                                        @Valid @ModelAttribute("claim") BurialClaim burialClaim,
                                        BindingResult result,
                                        @RequestParam(value = "documents", required = false) MultipartFile[] documents,
                                        @RequestParam(value = "documentTypes", required = false) String[] documentTypes,
                                        Model model,
                                        RedirectAttributes redirectAttributes) {

        if (memberId == null) {
            model.addAttribute("memberError", "Please select a member");
        }

        if (result.hasErrors() || memberId == null) {
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            return "claim/new";
        }

        try {
            BurialClaim saved = burialClaimService.createClaim(memberId, burialClaim);

            // Upload documents
            if (documents != null) {
                for (int i = 0; i < documents.length; i++) {
                    if (!documents[i].isEmpty()) {
                        String docType = (documentTypes != null && i < documentTypes.length) ? documentTypes[i] : "SUPPORTING_DOCUMENT";
                        documentService.uploadForClaim(documents[i], saved.getId(), docType);
                    }
                }
            }
        } catch (MemberMissedLastThreeConsecutiveMonthsException ex) {
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "claim/new";
        } catch (Exception ex) {
            LOGGER.error("Error uploading documents for claim", ex);
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            model.addAttribute("errorMessage", "Claim created but document upload failed: " + ex.getMessage());
            return "claim/new";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Burial claim submitted successfully!");
        return "redirect:/claims";
    }

    // ==================
    // NEW CLAIM (from member)
    // ==================
    @GetMapping("/members/{memberId}/claims/new")
    public String form(@PathVariable Long memberId, Model model) {
        model.addAttribute("pageTitle", "New Burial Claim");
        model.addAttribute("member", memberService.getActiveMemberById(memberId));
        model.addAttribute("claim", new BurialClaim());
        return "claim/form";
    }

    @PostMapping("/members/{memberId}/claims")
    public String create(@PathVariable Long memberId,
                         @Valid @ModelAttribute("claim") BurialClaim burialClaim,
                         BindingResult result,
                         @RequestParam(value = "documents", required = false) MultipartFile[] documents,
                         @RequestParam(value = "documentTypes", required = false) String[] documentTypes,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            return "claim/form";
        }

        try {
            BurialClaim saved = burialClaimService.createClaim(memberId, burialClaim);

            if (documents != null) {
                for (int i = 0; i < documents.length; i++) {
                    if (!documents[i].isEmpty()) {
                        String docType = (documentTypes != null && i < documentTypes.length) ? documentTypes[i] : "SUPPORTING_DOCUMENT";
                        documentService.uploadForClaim(documents[i], saved.getId(), docType);
                    }
                }
            }
        } catch (MemberMissedLastThreeConsecutiveMonthsException ex) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "claim/form";
        } catch (Exception ex) {
            LOGGER.error("Error uploading documents for claim", ex);
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            model.addAttribute("errorMessage", "Claim created but document upload failed: " + ex.getMessage());
            return "claim/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Burial claim submitted successfully!");
        return "redirect:/members/" + memberId;
    }

    // ==================
    // APPROVE / DECLINE
    // ==================
    @PostMapping("/claims/{claimId}/approve")
    public String approve(@PathVariable Long claimId, RedirectAttributes redirectAttributes) {
        burialClaimService.approveClaim(claimId);
        redirectAttributes.addFlashAttribute("successMessage", "Claim approved successfully!");
        return "redirect:/claims";
    }

    @PostMapping("/claims/{claimId}/decline")
    public String decline(@PathVariable Long claimId, RedirectAttributes redirectAttributes) {
        burialClaimService.declineClaim(claimId);
        redirectAttributes.addFlashAttribute("errorMessage", "Claim has been declined.");
        return "redirect:/claims";
    }
}
