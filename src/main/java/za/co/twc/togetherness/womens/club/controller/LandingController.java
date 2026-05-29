package za.co.twc.togetherness.womens.club.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class LandingController {

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
}
