package za.co.twc.togetherness.womens.club.service;

/**
 * Abstraction for sending emails.
 * Implementations can use SMTP (local dev) or Resend API (production).
 */
public interface EmailService {

    void sendEmail(String to, String subject, String body);
}
