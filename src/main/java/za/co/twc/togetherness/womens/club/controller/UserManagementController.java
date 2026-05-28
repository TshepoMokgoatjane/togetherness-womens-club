package za.co.twc.togetherness.womens.club.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;
import za.co.twc.togetherness.womens.club.service.MemberService;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/users")
public class UserManagementController {

    private static final Logger LOGGER = LoggerFactory.getLogger(UserManagementController.class);

    private final UserRepository userRepository;
    private final MemberService memberService;

    public UserManagementController(UserRepository userRepository, MemberService memberService) {
        this.userRepository = userRepository;
        this.memberService = memberService;
    }

    @GetMapping
    public String listUsers(Model model) {
        List<User> users = userRepository.findAll();
        List<Member> members = memberService.getAllActiveMembers();

        model.addAttribute("users", users);
        model.addAttribute("members", members);
        return "admin/users";
    }

    @PostMapping("/{userId}/link")
    public String linkUserToMember(@PathVariable Long userId,
                                   @RequestParam Long memberId,
                                   RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();

        // Check if this member is already linked to another user
        Optional<User> existingLink = userRepository.findByMemberId(memberId);
        if (existingLink.isPresent() && !existingLink.get().getId().equals(userId)) {
            redirectAttributes.addFlashAttribute("errorMessage",
                    "This member is already linked to user: " + existingLink.get().getUsername());
            return "redirect:/admin/users";
        }

        Member member = memberService.getActiveMemberById(memberId);

        user.setMemberId(memberId);
        userRepository.save(user);

        LOGGER.info("User '{}' linked to member '{}' ({})", user.getUsername(),
                member.getFirstName() + " " + member.getLastName(), member.getMemberNumber());

        redirectAttributes.addFlashAttribute("successMessage",
                "User '" + user.getUsername() + "' linked to member " + member.getFirstName() + " " + member.getLastName() + ".");

        return "redirect:/admin/users";
    }

    @PostMapping("/{userId}/unlink")
    public String unlinkUser(@PathVariable Long userId, RedirectAttributes redirectAttributes) {

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/admin/users";
        }

        User user = userOpt.get();
        user.setMemberId(null);
        userRepository.save(user);

        LOGGER.info("User '{}' unlinked from member", user.getUsername());

        redirectAttributes.addFlashAttribute("successMessage",
                "User '" + user.getUsername() + "' has been unlinked from their member record.");

        return "redirect:/admin/users";
    }
}
