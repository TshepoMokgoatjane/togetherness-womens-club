package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.exception.DuplicateMonthlyContributionException;
import za.co.twc.togetherness.womens.club.exception.InvalidContributionAmountException;
import za.co.twc.togetherness.womens.club.exception.NonActiveMemberContributionException;
import za.co.twc.togetherness.womens.club.service.ContributionService;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
public class ContributionController {

    private final ContributionService contributionService;
    private final MemberService memberService;

    @Autowired
    public ContributionController(ContributionService contributionService, MemberService memberService) {
        this.contributionService = contributionService;
        this.memberService = memberService;
    }

    // ==================
    // ALL CONTRIBUTIONS
    // ==================
    @GetMapping("/contributions")
    public String listAll(Model model) {
        model.addAttribute("pageTitle", "All Contributions");
        model.addAttribute("contributions", contributionService.getAllContributions());
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

        contributionService.createContribution(memberId, contribution);
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
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (contribution.getReference() == null || contribution.getReference().isBlank()) {
            result.rejectValue("reference", "error.contribution", "Reference is required");
        }

        if (result.hasErrors()) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            return "contribution/form";
        }

        contributionService.createContribution(memberId, contribution);
        redirectAttributes.addFlashAttribute("successMessage", "Contribution recorded successfully!");
        return "redirect:/members/" + memberId + "/contributions";
    }

    // ==================
    // EXCEPTION HANDLING
    // ==================
    @ExceptionHandler(DuplicateMonthlyContributionException.class)
    public String handleDuplicateContribution(DuplicateMonthlyContributionException ex,
                                              RedirectAttributes redirectAttributes,
                                              jakarta.servlet.http.HttpServletRequest request) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        String referer = request.getHeader("Referer");
        return "redirect:" + (referer != null ? referer : "/contributions");
    }

    @ExceptionHandler(NonActiveMemberContributionException.class)
    public String handleNonActiveMember(NonActiveMemberContributionException ex,
                                        RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/contributions/new";
    }

    @ExceptionHandler(InvalidContributionAmountException.class)
    public String handleInvalidAmount(InvalidContributionAmountException ex,
                                      RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/contributions/new";
    }
}
