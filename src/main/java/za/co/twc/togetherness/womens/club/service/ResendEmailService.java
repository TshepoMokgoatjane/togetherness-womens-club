package za.co.twc.togetherness.womens.club.service;

import com.resend.Resend;
import com.resend.core.exception.ResendException;
import com.resend.services.emails.model.CreateEmailOptions;
import com.resend.services.emails.model.CreateEmailResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Resend API-based email service for production.
 * Uses HTTP (port 443) so it works on Railway and other platforms that block SMTP.
 */
@Service
@Profile("prod")
public class ResendEmailService implements EmailService {

    private static final Logger LOGGER = LoggerFactory.getLogger(ResendEmailService.class);

    private final Resend resend;
    private final String fromEmail;

    public ResendEmailService(@Value("${resend.api-key}") String apiKey,
                              @Value("${resend.from-email}") String fromEmail) {
        this.resend = new Resend(apiKey);
        this.fromEmail = fromEmail;
    }

    @Override
    public void sendEmail(String to, String subject, String body) {
        LOGGER.info("Sending email via Resend to {}", to);

        CreateEmailOptions params = CreateEmailOptions.builder()
                .from(fromEmail)
                .to(to)
                .subject(subject)
                .text(body)
                .build();

        try {
            CreateEmailResponse response = resend.emails().send(params);
            LOGGER.info("Email sent successfully via Resend to {} (id: {})", to, response.getId());
        } catch (ResendException e) {
            LOGGER.error("Failed to send email via Resend to {}: {}", to, e.getMessage());
            throw new RuntimeException("Unable to send email. Please try again later.", e);
        }
    }
}
