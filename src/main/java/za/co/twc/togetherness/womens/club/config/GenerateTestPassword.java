package za.co.twc.togetherness.womens.club.config;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

public class GenerateTestPassword {
    public static void main(String[] args) {
        BCryptPasswordEncoder bCryptPasswordEncoder = new BCryptPasswordEncoder();
        String hashedPassword = bCryptPasswordEncoder.encode("user123");
        System.out.println(hashedPassword);
    }
}
