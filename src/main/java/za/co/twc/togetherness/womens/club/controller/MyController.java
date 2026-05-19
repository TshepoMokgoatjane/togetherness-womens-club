package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.RelationshipType;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;
import za.co.twc.togetherness.womens.club.service.BurialClaimService;
import za.co.twc.togetherness.womens.club.service.ContributionService;
import za.co.twc.togetherness.womens.club.service.DependentService;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
@RequestMapping("/my")
public class MyController {

    private final UserRepository userRepository;
    private final MemberService memberService;
    private final DependentService dependentService;
    private final ContributionService contributionService;
    private final BurialClaimService burialClaimService;

    public MyController(UserRepository userRepository,
                        MemberService memberService,
                        DependentService dependentService,
                        ContributionService contributionService,
                        BurialClaimService burialClaimService) {
        this.userRepository = userRepository;
        this.memberService = memberService;
        this.dependentService = dependentService;
        this.contributionService = contributionService;
        this.burialClaimService = burialClaimService;
    }

    // ==================
    // MY PROFILE (Member details)
    // ==================
    @GetMapping
    public String myDashboard(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) {
            model.addAttribute("errorMessage", "Your account is not linked to a member. Please contact an administrator.");
            return "my/not-linked";
        }
        model.addAttribute("member", member);
        model.addAttribute("dependents", dependentService.getDependentsByMemberId(member.getId()));
        model.addAttribute("contributions", contributionService.getContributionsByMemberId(member.getId()));
        return "my/dashboard";
    }

    // ==================
    // MY DEPENDENTS
    // ==================
    @GetMapping("/dependents")
    public String myDependents(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";
        model.addAttribute("member", member);
        model.addAttribute("dependents", dependentService.getDependentsByMemberId(member.getId()));
        return "my/dependents";
    }

    @GetMapping("/dependents/new")
    public String newDependentForm(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";
        model.addAttribute("member", member);
        model.addAttribute("dependent", new Dependent());
        model.addAttribute("relationships", RelationshipType.values());
        return "my/dependent-form";
    }

    @PostMapping("/dependents")
    public String addDependent(Authentication authentication,
                               @Valid @ModelAttribute("dependent") Dependent dependent,
                               BindingResult result,
                               Model model,
                               RedirectAttributes redirectAttributes) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";

        if (result.hasErrors()) {
            model.addAttribute("member", member);
            model.addAttribute("relationships", RelationshipType.values());
            return "my/dependent-form";
        }

        dependentService.createDependent(member.getId(), dependent);
        redirectAttributes.addFlashAttribute("successMessage", "Dependent added successfully!");
        return "redirect:/my/dependents";
    }

    // ==================
    // MY CONTRIBUTIONS
    // ==================
    @GetMapping("/contributions")
    public String myContributions(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";
        model.addAttribute("member", member);
        model.addAttribute("contributions", contributionService.getContributionsByMemberId(member.getId()));
        return "my/contributions";
    }

    // ==================
    // MY CLAIMS
    // ==================
    @GetMapping("/claims")
    public String myClaims(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";
        model.addAttribute("member", member);
        model.addAttribute("claims", burialClaimService.getClaimsByMemberId(member.getId()));
        return "my/claims";
    }

    @GetMapping("/claims/new")
    public String newClaimForm(Authentication authentication, Model model) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";
        model.addAttribute("member", member);
        model.addAttribute("claim", new BurialClaim());
        return "my/claim-form";
    }

    @PostMapping("/claims")
    public String submitClaim(Authentication authentication,
                              @Valid @ModelAttribute("claim") BurialClaim claim,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirectAttributes) {
        Member member = getLoggedInMember(authentication);
        if (member == null) return "redirect:/my";

        if (result.hasErrors()) {
            model.addAttribute("member", member);
            return "my/claim-form";
        }

        burialClaimService.createClaim(member.getId(), claim);
        redirectAttributes.addFlashAttribute("successMessage", "Claim submitted successfully!");
        return "redirect:/my/claims";
    }

    // ==================
    // HELPER
    // ==================
    private Member getLoggedInMember(Authentication authentication) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);
        if (user == null || user.getMemberId() == null) return null;
        try {
            return memberService.getActiveMemberById(user.getMemberId());
        } catch (Exception e) {
            return null;
        }
    }
}
