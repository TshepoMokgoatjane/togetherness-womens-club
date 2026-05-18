package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.ResetPasswordForm;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;

import java.util.Optional;

@Controller
@RequestMapping("/forgot-password")
public class ForgotPasswordController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public ForgotPasswordController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showResetForm(Model model) {
        model.addAttribute("resetPasswordForm", new ResetPasswordForm());
        return "forgot-password";
    }

    @PostMapping
    public String resetPassword(@Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                                BindingResult result,
                                RedirectAttributes redirectAttributes) {

        // Check passwords match
        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.resetPasswordForm", "Passwords do not match");
        }

        // Check user exists
        Optional<User> userOpt = userRepository.findByUsername(form.getUsername());
        if (userOpt.isEmpty()) {
            result.rejectValue("username", "error.resetPasswordForm", "Username not found");
        }

        if (result.hasErrors()) {
            return "forgot-password";
        }

        // Update password
        User user = userOpt.get();
        user.setPassword(passwordEncoder.encode(form.getNewPassword()));
        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Password reset successful! Please login with your new password.");
        return "redirect:/login";
    }
}
