package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.exception.DuplicateMonthlyContributionException;
import za.co.twc.togetherness.womens.club.exception.InvalidContributionAmountException;
import za.co.twc.togetherness.womens.club.exception.NonActiveMemberContributionException;
import za.co.twc.togetherness.womens.club.service.ContributionService;
import za.co.twc.togetherness.womens.club.service.DocumentService;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
public class ContributionController {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContributionController.class);

    private final ContributionService contributionService;
    private final MemberService memberService;
    private final DocumentService documentService;

    @Autowired
    public ContributionController(ContributionService contributionService, MemberService memberService, DocumentService documentService) {
        this.contributionService = contributionService;
        this.memberService = memberService;
        this.documentService = documentService;
    }

    // ==================
    // ALL CONTRIBUTIONS
    // ==================
    @GetMapping("/contributions")
    public String listAll(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "paymentDate") String sortBy,
            @RequestParam(required = false) String search,
            @RequestParam(required = false) za.co.twc.togetherness.womens.club.domain.ContributionStatus status,
            Model model) {

        Page<Contribution> pageData = contributionService.searchAndFilterContributions(page, size, sortBy, search, status);

        model.addAttribute("pageTitle", "All Contributions");
        model.addAttribute("contributions", pageData.getContent());
        model.addAttribute("currentPage", page);
        model.addAttribute("totalPages", pageData.getTotalPages());
        model.addAttribute("totalItems", pageData.getTotalElements());
        model.addAttribute("pageSize", size);
        model.addAttribute("sortBy", sortBy);
        model.addAttribute("search", search);
        model.addAttribute("status", status);

        // Load documents for each contribution
        java.util.Map<Long, java.util.List<za.co.twc.togetherness.womens.club.domain.Document>> contributionDocuments = new java.util.HashMap<>();
        for (Contribution c : pageData.getContent()) {
            contributionDocuments.put(c.getId(), documentService.getDocumentsForContribution(c.getId()));
        }
        model.addAttribute("contributionDocuments", contributionDocuments);

        return "contribution/all";
    }

    // =============================================
    // ADD CONTRIBUTION (standalone - select member)
    // =============================================
    @GetMapping("/contributions/new")
    public String newContributionForm(Model model) {
        model.addAttribute("pageTitle", "New Contribution");
        model.addAttribute("members", memberService.getAllActiveMembers());
        model.addAttribute("contribution", new Contribution());
        return "contribution/new";
    }

    @PostMapping("/contributions")
    public String createContribution(@RequestParam(required = false) Long memberId,
                                     @Valid @ModelAttribute("contribution") Contribution contribution,
                                     BindingResult result,
                                     @RequestParam(value = "proofOfPayment", required = false) MultipartFile proofOfPayment,
                                     Model model,
                                     RedirectAttributes redirectAttributes) {

        if (memberId == null) {
            model.addAttribute("memberError", "Please select a member");
        }

        if (contribution.getReference() == null || contribution.getReference().isBlank()) {
            result.rejectValue("reference", "error.contribution", "Reference is required");
        }

        if (result.hasErrors() || memberId == null) {
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            return "contribution/new";
        }

        try {
            Contribution saved = contributionService.createContribution(memberId, contribution);

            if (proofOfPayment != null && !proofOfPayment.isEmpty()) {
                documentService.uploadForContribution(proofOfPayment, saved.getId());
            }
        } catch (DuplicateMonthlyContributionException | NonActiveMemberContributionException |
                 InvalidContributionAmountException ex) {
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            model.addAttribute("errorMessage", ex.getMessage());
            return "contribution/new";
        } catch (Exception ex) {
            LOGGER.error("Error uploading proof of payment", ex);
            model.addAttribute("members", memberService.getAllActiveMembers());
            model.addAttribute("selectedMemberId", memberId);
            model.addAttribute("errorMessage", "Contribution saved but document upload failed: " + ex.getMessage());
            return "contribution/new";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Contribution recorded successfully!");
        return "redirect:/contributions";
    }

    // ====================
    // MEMBER CONTRIBUTIONS
    // ====================
    @GetMapping("/members/{memberId}/contributions")
    public String list(@PathVariable Long memberId, Model model) {
        model.addAttribute("pageTitle", "Member Contributions");
        model.addAttribute("member", memberService.getActiveMemberById(memberId));
        model.addAttribute("contributions", contributionService.getContributionsByMemberId(memberId));
        return "contribution/list";
    }

    @GetMapping("/members/{memberId}/contributions/new")
    public String form(@PathVariable Long memberId, Model model) {
        model.addAttribute("pageTitle", "New Contribution");
        model.addAttribute("member", memberService.getActiveMemberById(memberId));
        model.addAttribute("contribution", new Contribution());
        return "contribution/form";
    }

    @PostMapping("/members/{memberId}/contributions")
    public String create(@PathVariable Long memberId,
                         @Valid @ModelAttribute("contribution") Contribution contribution,
                         BindingResult result,
                         @RequestParam(value = "proofOfPayment", required = false) MultipartFile proofOfPayment,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (contribution.getReference() == null || contribution.getReference().isBlank()) {
            result.rejectValue("reference", "error.contribution", "Reference is required");
        }

        if (result.hasErrors()) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            return "contribution/form";
        }

        try {
            Contribution saved = contributionService.createContribution(memberId, contribution);

            if (proofOfPayment != null && !proofOfPayment.isEmpty()) {
                documentService.uploadForContribution(proofOfPayment, saved.getId());
            }
        } catch (DuplicateMonthlyContributionException | NonActiveMemberContributionException |
                 InvalidContributionAmountException ex) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            model.addAttribute("errorMessage", ex.getMessage());
            return "contribution/form";
        } catch (Exception ex) {
            LOGGER.error("Error uploading proof of payment", ex);
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            model.addAttribute("errorMessage", "Contribution saved but document upload failed: " + ex.getMessage());
            return "contribution/form";
        }

        redirectAttributes.addFlashAttribute("successMessage", "Contribution recorded successfully!");
        return "redirect:/members/" + memberId + "/contributions";
    }

}
