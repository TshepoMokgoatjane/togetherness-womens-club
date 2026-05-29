package za.co.twc.togetherness.womens.club.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.event.EventListener;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.ClaimStatus;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.event.ClaimStatusChangedEvent;

@Component
public class ClaimStatusChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStatusChangedListener.class);

    private final JavaMailSender mailSender;

    public ClaimStatusChangedListener(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Async
    @EventListener
    public void handleClaimStatusChanged(ClaimStatusChangedEvent event) {
        BurialClaim claim = event.getClaim();
        ClaimStatus status = event.getClaimStatus();
        Member member = claim.getMember();

        if (member.getEmail() == null || member.getEmail().isBlank()) {
            LOGGER.warn("Cannot send claim notification - member {} has no email", member.getMemberNumber());
            return;
        }

        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setTo(member.getEmail());
            message.setSubject("Burial Claim " + status.name() + " - Togetherness Women's Club");

            String body;
            if (status == ClaimStatus.APPROVED) {
                body = String.format(
                        "Dear %s,\n\n" +
                        "Your burial claim for %s has been APPROVED.\n\n" +
                        "Claim Amount: R %.2f\n" +
                        "Claim Date: %s\n\n" +
                        "The payout will be processed shortly.\n\n" +
                        "Kind regards,\n" +
                        "Togetherness Women's Club",
                        member.getFirstName(),
                        claim.getDeceasedName(),
                        claim.getClaimAmount(),
                        claim.getClaimDate()
                );
            } else {
                body = String.format(
                        "Dear %s,\n\n" +
                        "We regret to inform you that your burial claim for %s has been DECLINED.\n\n" +
                        "Claim Amount: R %.2f\n" +
                        "Claim Date: %s\n\n" +
                        "Please contact the committee for more information.\n\n" +
                        "Kind regards,\n" +
                        "Togetherness Women's Club",
                        member.getFirstName(),
                        claim.getDeceasedName(),
                        claim.getClaimAmount(),
                        claim.getClaimDate()
                );
            }

            message.setText(body);
            mailSender.send(message);

            LOGGER.info("Claim {} notification sent to {} for member {}",
                    status, member.getEmail(), member.getMemberNumber());

        } catch (Exception e) {
            LOGGER.error("Failed to send claim notification to {}: {}", member.getEmail(), e.getMessage());
        }
    }
}
