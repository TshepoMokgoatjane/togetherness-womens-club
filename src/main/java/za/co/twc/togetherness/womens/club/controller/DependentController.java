package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Dependent;
import za.co.twc.togetherness.womens.club.domain.RelationshipType;
import za.co.twc.togetherness.womens.club.service.DependentService;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
@RequestMapping("/members/{memberId}/dependents")
public class DependentController {

    private final DependentService dependentService;
    private final MemberService memberService;

    public DependentController(DependentService dependentService, MemberService memberService) {
        this.dependentService = dependentService;
        this.memberService = memberService;
    }

    @GetMapping
    public String showDependents(@PathVariable("memberId") Long memberId, Model model) {
        model.addAttribute("member", memberService.getActiveMemberById(memberId));
        model.addAttribute("dependents", dependentService.getDependentsByMemberId(memberId));
        return "dependent/list";
    }

    @GetMapping("/new")
    public String showCreateForm(@PathVariable Long memberId, Model model) {
        model.addAttribute("member", memberService.getActiveMemberById(memberId));
        model.addAttribute("dependent", new Dependent());
        model.addAttribute("relationships", RelationshipType.values());
        return "dependent/form";
    }

    @PostMapping
    public String create(@PathVariable Long memberId,
                         @Valid @ModelAttribute Dependent dependent,
                         BindingResult result,
                         Model model,
                         RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            model.addAttribute("member", memberService.getActiveMemberById(memberId));
            model.addAttribute("relationships", RelationshipType.values());
            return "dependent/form";
        }

        dependentService.createDependent(memberId, dependent);

        redirectAttributes.addFlashAttribute("successMessage", "Dependent created successfully!");

        return "redirect:/members/" + memberId + "/dependents";
    }

    @PostMapping("/{dependentId}/delete")
    public String delete(@PathVariable Long memberId, @PathVariable Long dependentId, RedirectAttributes redirectAttributes) {
        dependentService.softDeleteDependent(dependentId);
        redirectAttributes.addFlashAttribute("successMessage", "Dependent deleted successfully!");
        return "redirect:/members/" + memberId + "/dependents";
    }
}
