package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.exception.MemberHasDependentsException;
import za.co.twc.togetherness.womens.club.exception.MemberNotFoundException;
import za.co.twc.togetherness.womens.club.service.DependentService;
import za.co.twc.togetherness.womens.club.service.MemberService;
import za.co.twc.togetherness.womens.club.utilities.SaIdUtils;

import java.time.LocalDate;
import java.time.Period;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;
    private final DependentService dependentService;

    public MemberController(MemberService memberService, DependentService dependentService) {
        this.memberService = memberService;
        this.dependentService = dependentService;
    }

    // ==================
    // LIST
    // ==================
    @GetMapping
    public String listMembers(Model model) {
        model.addAttribute("members", memberService.getAllActiveMembers());
        return "member/list";
    }

    // ==================
    // CREATE
    // ==================
    @GetMapping("/new")
    public String showCreateForm(Model model) {
        model.addAttribute("member", new Member());
        return "member/form";
    }

    @PostMapping
    public String createMember(@Valid @ModelAttribute("member") Member member,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "member/form";
        }

        memberService.createMember(member);

        redirectAttributes.addFlashAttribute("successMessage", "Member created successfully!");

        return "redirect:/members";
    }

    // ==================
    // VIEW
    // ==================
    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model) {

        Member member = memberService.getActiveMemberById(id);

        LocalDate dateOfBirth = SaIdUtils.extractDobFromId(member.getIdNumber());
        int age = Period.between(dateOfBirth, LocalDate.now()).getYears();

        model.addAttribute("member", member);
        model.addAttribute("dependents", dependentService.getDependentsByMemberId(id));
        model.addAttribute("age", age);
        model.addAttribute("dateOfBirth", dateOfBirth);

        return "member/view";
    }

    // ==================
    // UPDATE
    // ==================
    @GetMapping("/{id}/edit")
    public String showUpdateForm(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.getActiveMemberById(id));
        return "member/form";
    }

    @PostMapping("/{id}")
    public String updateMember(@PathVariable Long id,
                               @Valid @ModelAttribute("member") Member member,
                               BindingResult result,
                               RedirectAttributes redirectAttributes) {

        if (result.hasErrors()) {
            return "member/form";
        }

        memberService.updateMember(id, member);

        redirectAttributes.addFlashAttribute("successMessage", "Member updated successfully!");

        return "redirect:/members";
    }

    // ==================
    // DELETE (SOFT)
    // ==================
    @PostMapping("/{id}/delete")
    public String deleteMember(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        memberService.softDeleteMember(id);
        redirectAttributes.addFlashAttribute("successMessage", "Member deleted successfully!");
        return "redirect:/members";
    }

    // ==================
    // EXCEPTION HANDLING
    // ==================
    @ExceptionHandler(MemberNotFoundException.class)
    public String handleMemberNotFoundException(MemberNotFoundException ex, Model model) {
        model.addAttribute("errorMessage", ex.getMessage());
        return "error/404";
    }

    // ====================================
    // CANNOT DELETE MEMBER WITH DEPENDENTS
    // ====================================
    @ExceptionHandler(MemberHasDependentsException.class)
    public String handleMemberHasDependents(MemberHasDependentsException ex, RedirectAttributes redirectAttributes) {
        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        return "redirect:/members";
    }
}
