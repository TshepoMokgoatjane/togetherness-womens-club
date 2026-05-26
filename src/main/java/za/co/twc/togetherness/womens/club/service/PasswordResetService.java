package za.co.twc.togetherness.womens.club.service;

import org.jspecify.annotations.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import za.co.twc.togetherness.womens.club.domain.PasswordResetToken;
import za.co.twc.togetherness.womens.club.domain.User;
import za.co.twc.togetherness.womens.club.exception.EmailTokenExpiredException;
import za.co.twc.togetherness.womens.club.exception.FailedToSendPasswordResetEmailException;
import za.co.twc.togetherness.womens.club.exception.InvalidEmailTokenException;
import za.co.twc.togetherness.womens.club.repository.PasswordResetTokenRepository;
import za.co.twc.togetherness.womens.club.repository.UserRepository;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Service
public class PasswordResetService {

    private static final Logger LOGGER = LoggerFactory.getLogger(PasswordResetService.class);

    private final PasswordResetTokenRepository passwordResetTokenRepository;
    private final UserRepository userRepository;
    private final JavaMailSender javaMailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    public PasswordResetService(PasswordResetTokenRepository passwordResetTokenRepository,
                                UserRepository userRepository,
                                JavaMailSender javaMailSender) {
        this.passwordResetTokenRepository = passwordResetTokenRepository;
        this.userRepository = userRepository;
        this.javaMailSender = javaMailSender;
    }

    public void createPasswordResetToken(String email) {
        LOGGER.info("Creating password reset token for email {}", email);

        Optional<User> userOptional = userRepository.findByEmail(email);

        if (userOptional.isEmpty()) {
            LOGGER.warn("No user found with email {}", email);
            return; // Don't reveal whether user exists
        }

        User user = userOptional.get();

        String token = UUID.randomUUID().toString();

        PasswordResetToken passwordResetToken = new PasswordResetToken();
        passwordResetToken.setToken(token);
        passwordResetToken.setUser(user);
        passwordResetToken.setExpiryDate(LocalDateTime.now().plusMinutes(30));

        passwordResetTokenRepository.save(passwordResetToken);

        sendResetEmail(user.getEmail(), token);
    }

    private void sendResetEmail(String email, String token) {
        LOGGER.info("Sending password reset email to {}", email);

        SimpleMailMessage message = getSimpleMailMessage(email, token);

        try {
            javaMailSender.send(message);
            LOGGER.info("Password reset email sent successfully to {}", email);
        } catch (Exception e) {
            LOGGER.error("Failed to send password reset email to {}: {}", email, e.getMessage());
            throw new FailedToSendPasswordResetEmailException(e.getMessage());
        }
    }

    private @NonNull SimpleMailMessage getSimpleMailMessage(String email, String token) {
        String resetUrl = baseUrl + "/reset-password?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(email);
        message.setSubject("Togetherness Women's Club - Password Reset");
        message.setText("Hello,\n\nYou requested a password reset. Click the link below to reset your password:\n\n"
                + resetUrl
                + "\n\nThis link expires in 30 minutes.\n\nIf you did not request this, please ignore this email.\n\n"
                + "Togetherness Women's Club");
        return message;
    }

    public PasswordResetToken validateToken(String token) {
        LOGGER.info("Validating password reset token");

        PasswordResetToken passwordResetToken = passwordResetTokenRepository.findByToken(token)
                .orElseThrow(() -> new InvalidEmailTokenException(token));

        if (passwordResetToken.getExpiryDate().isBefore(LocalDateTime.now())) {
            throw new EmailTokenExpiredException(token);
        }

        return passwordResetToken;
    }

    public void resetPassword(String token, String newPassword, PasswordEncoder encoder) {
        LOGGER.info("Resetting password via token");

        PasswordResetToken passwordResetToken = validateToken(token);

        User user = passwordResetToken.getUser();
        user.setPassword(encoder.encode(newPassword));
        userRepository.save(user);

        // One-time use — delete token after use
        passwordResetTokenRepository.delete(passwordResetToken);
    }
}
