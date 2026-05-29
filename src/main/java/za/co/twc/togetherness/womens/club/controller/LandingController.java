package za.co.twc.togetherness.womens.club.controller;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class LandingController {

    private static final Logger LOGGER = LoggerFactory.getLogger(LandingController.class);

    private final JavaMailSender mailSender;

    public LandingController(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @GetMapping("/")
    public String landing() {
        return "public/landing";
    }

    @GetMapping("/about")
    public String about() {
        return "public/about";
    }

    @GetMapping("/gallery")
    public String gallery() {
        return "public/gallery";
    }

    @GetMapping("/contact")
    public String contact() {
        return "public/contact";
    }

    @PostMapping("/contact")
    public String submitContactForm(@RequestParam String name,
                                    @RequestParam String email,
                                    @RequestParam String subject,
                                    @RequestParam String message,
                                    RedirectAttributes redirectAttributes) {
        try {
            SimpleMailMessage mailMessage = new SimpleMailMessage();
            mailMessage.setTo("tshepomokgoatjane11@gmail.com");
            mailMessage.setSubject("Contact Form: " + subject);
            mailMessage.setReplyTo(email);
            mailMessage.setText(
                    "New message from the Contact Us form:\n\n" +
                    "Name: " + name + "\n" +
                    "Email: " + email + "\n" +
                    "Subject: " + subject + "\n\n" +
                    "Message:\n" + message
            );

            mailSender.send(mailMessage);

            LOGGER.info("Contact form message sent from {} ({})", name, email);
            redirectAttributes.addFlashAttribute("successMessage", "Your message has been sent successfully! We'll get back to you soon.");

        } catch (Exception e) {
            LOGGER.error("Failed to send contact form message from {}: {}", email, e.getMessage());
            redirectAttributes.addFlashAttribute("errorMessage", "Sorry, we couldn't send your message. Please try again later.");
        }

        return "redirect:/contact";
    }
}
