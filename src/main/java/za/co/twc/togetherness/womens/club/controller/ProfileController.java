package za.co.twc.togetherness.womens.club.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;

@Controller
@RequestMapping("/profile")
public class ProfileController {

    private final UserRepository userRepository;

    public ProfileController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String viewProfile(Authentication authentication, Model model) {
        String username = authentication.getName();
        User user = userRepository.findByUsername(username).orElse(null);

        model.addAttribute("user", user);
        return "profile/view";
    }
}
