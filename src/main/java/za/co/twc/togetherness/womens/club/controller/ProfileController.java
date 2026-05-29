package za.co.twc.togetherness.womens.club.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ProfileController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        model.addAttribute("user", user);
        return "profile/view";
    }

    @GetMapping("/change-password")
    public String showChangePasswordForm() {
        return "profile/change-password";
    }

    @PostMapping("/change-password")
    public String changePassword(Authentication authentication,
                                 @RequestParam String currentPassword,
                                 @RequestParam String newPassword,
                                 @RequestParam String confirmPassword,
                                 RedirectAttributes redirectAttributes) {

        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        if (user == null) {
            redirectAttributes.addFlashAttribute("errorMessage", "User not found.");
            return "redirect:/profile/change-password";
        }

        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            redirectAttributes.addFlashAttribute("errorMessage", "Current password is incorrect.");
            return "redirect:/profile/change-password";
        }

        if (newPassword.length() < 6) {
            redirectAttributes.addFlashAttribute("errorMessage", "New password must be at least 6 characters.");
            return "redirect:/profile/change-password";
        }

        if (!newPassword.equals(confirmPassword)) {
            redirectAttributes.addFlashAttribute("errorMessage", "New passwords do not match.");
            return "redirect:/profile/change-password";
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password changed successfully!");
        return "redirect:/profile";
    }
}
