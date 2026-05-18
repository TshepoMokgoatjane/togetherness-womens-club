package za.co.twc.togetherness.womens.club.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LoginController {

    @GetMapping("/login")
    public String login() {
        return "login";
    }

    @GetMapping("/login-success")
    public String loginSuccess(org.springframework.security.core.Authentication authentication) {
        // Route ADMIN and TREASURER to members page, USER to profile
        boolean isAdminOrTreasurer = authentication.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN")
                        || auth.getAuthority().equals("ROLE_TREASURER"));

        if (isAdminOrTreasurer) {
            return "redirect:/members";
        }
        return "redirect:/profile";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
