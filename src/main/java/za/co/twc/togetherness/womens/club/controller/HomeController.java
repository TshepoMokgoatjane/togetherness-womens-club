package za.co.twc.togetherness.womens.club.controller;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;
import za.co.twc.togetherness.womens.club.repository.BurialClaimRepository;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
public class HomeController {

    private final MemberService memberService;
    private final ContributionRepository contributionRepository;
    private final BurialClaimRepository burialClaimRepository;

    public HomeController(MemberService memberService,
                          ContributionRepository contributionRepository,
                          BurialClaimRepository burialClaimRepository) {
        this.memberService = memberService;
        this.contributionRepository = contributionRepository;
        this.burialClaimRepository = burialClaimRepository;
    }

    @GetMapping("/home")
    public String home(Model model) {
        model.addAttribute("pageTitle", "Home");
        model.addAttribute("totalMembers", memberService.getAllActiveMembers().size());
        model.addAttribute("totalContributions", contributionRepository.count());
        model.addAttribute("pendingClaims", burialClaimRepository.countByStatus(ClaimStatus.PENDING));
        return "home";
    }
}
