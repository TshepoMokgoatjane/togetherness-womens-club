package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.exception.MemberNotFoundException;
import za.co.twc.togetherness.womens.club.service.MemberService;

@Controller
@RequestMapping("/members")
public class MemberController {

    private final MemberService memberService;

    public MemberController(MemberService memberService) {
        this.memberService = memberService;
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
    public String createMember(@Valid @ModelAttribute("member") Member member, BindingResult result) {

        if (result.hasErrors()) {
            return "member/form";
        }

        memberService.createMember(member);
        return "redirect:/members";
    }

    // ==================
    // VIEW
    // ==================
    @GetMapping("/{id}")
    public String viewMember(@PathVariable Long id, Model model) {
        model.addAttribute("member", memberService.getActiveMemberById(id));
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
    public String updateMember(@PathVariable Long id, @Valid @ModelAttribute("member") Member member, BindingResult result) {

        if (result.hasErrors()) {
            return "member/form";
        }

        memberService.updateMember(id, member);
        return "redirect:/members";
    }

    // ==================
    // DELETE (SOFT)
    // ==================
    @PostMapping("/{id}/delete")
    public String deleteMember(@PathVariable Long id) {
        memberService.softDeleteMember(id);
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
}
