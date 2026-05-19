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
        return "redirect:/home";
    }

    @GetMapping("/access-denied")
    public String accessDenied() {
        return "error/403";
    }
}
