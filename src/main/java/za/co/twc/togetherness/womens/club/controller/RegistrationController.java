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
import za.co.twc.togetherness.womens.club.domain.RegistrationForm;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.repository.UserRepository;

@Controller
@RequestMapping("/register")
public class RegistrationController {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public RegistrationController(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @GetMapping
    public String showRegistrationForm(Model model) {
        model.addAttribute("registrationForm", new RegistrationForm());
        return "register";
    }

    @PostMapping
    public String register(@Valid @ModelAttribute("registrationForm") RegistrationForm form,
                           BindingResult result,
                           RedirectAttributes redirectAttributes) {

        // Check passwords match
        if (!form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "error.registrationForm", "Passwords do not match");
        }

        // Check username not taken
        if (userRepository.findByUsername(form.getUsername()).isPresent()) {
            result.rejectValue("username", "error.registrationForm", "Username is already taken");
        }

        if (result.hasErrors()) {
            return "register";
        }

        // Create user with USER role
        User user = new User();
        user.setUsername(form.getUsername());
        user.setPassword(passwordEncoder.encode(form.getPassword()));
        user.setRole("USER");

        userRepository.save(user);

        redirectAttributes.addFlashAttribute("successMessage", "Registration successful! Please login.");
        return "redirect:/login";
    }
}
