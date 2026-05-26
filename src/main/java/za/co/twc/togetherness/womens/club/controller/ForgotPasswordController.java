package za.co.twc.togetherness.womens.club.controller;

import jakarta.validation.Valid;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import za.co.twc.togetherness.womens.club.domain.ResetPasswordForm;
import za.co.twc.togetherness.womens.club.exception.EmailTokenExpiredException;
import za.co.twc.togetherness.womens.club.exception.InvalidEmailTokenException;
import za.co.twc.togetherness.womens.club.service.PasswordResetService;

@Controller
public class ForgotPasswordController {

    private final PasswordEncoder passwordEncoder;
    private final PasswordResetService passwordResetService;

    public ForgotPasswordController(PasswordEncoder passwordEncoder, PasswordResetService passwordResetService) {
        this.passwordEncoder = passwordEncoder;
        this.passwordResetService = passwordResetService;
    }

    // Step 1: Show email form
    @GetMapping("/forgot-password")
    public String showForgotPasswordForm() {
        return "forgot-password";
    }

    // Step 2: Process email submission (send reset link)
    @PostMapping("/forgot-password")
    public String processForgotPassword(@RequestParam String email, RedirectAttributes redirectAttributes) {
        try {
            passwordResetService.createPasswordResetToken(email);
            redirectAttributes.addFlashAttribute("successMessage", "If an account with that email exists, a reset link has been sent.");
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Unable to send reset email. Please try again later.");
        }
        return "redirect:/login";
    }

    // Step 3: Show reset password form (user clicks link from email)
    @GetMapping("/reset-password")
    public String showResetPasswordForm(@RequestParam String token, Model model) {
        try {
            passwordResetService.validateToken(token);
            model.addAttribute("token", token);
            model.addAttribute("resetPasswordForm", new ResetPasswordForm());
            return "reset-password";
        } catch (InvalidEmailTokenException | EmailTokenExpiredException e) {
            model.addAttribute("errorMessage", e.getMessage());
            return "reset-password";
        }
    }

    // Step 4: Process new password
    @PostMapping("/reset-password")
    public String resetPassword(@RequestParam String token,
                                @Valid @ModelAttribute("resetPasswordForm") ResetPasswordForm form,
                                BindingResult result,
                                Model model,
                                RedirectAttributes redirectAttributes) {

        if (!form.getNewPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.resetPasswordForm", "Passwords do not match");
        }

        if (result.hasErrors()) {
            model.addAttribute("token", token);
            return "reset-password";
        }

        try {
            passwordResetService.resetPassword(token, form.getNewPassword(), passwordEncoder);
            redirectAttributes.addFlashAttribute("successMessage", "Password reset successful! Please login with your new password.");
            return "redirect:/login";
        } catch (InvalidEmailTokenException | EmailTokenExpiredException e) {
            redirectAttributes.addFlashAttribute("errorMessage", e.getMessage());
            return "redirect:/forgot-password";
        }
    }
}
