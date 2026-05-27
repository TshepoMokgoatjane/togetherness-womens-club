package za.co.twc.togetherness.womens.club.listener;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;
import za.co.twc.togetherness.womens.club.domain.BurialClaim;
import za.co.twc.togetherness.womens.club.domain.Member;
import za.co.twc.togetherness.womens.club.event.ClaimStatusChangedEvent;
import za.co.twc.togetherness.womens.club.service.EmailService;

@Component
public class ClaimStatusChangedListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(ClaimStatusChangedListener.class);

    private final EmailService emailService;

    public ClaimStatusChangedListener(EmailService emailService) {
        this.emailService = emailService;
    }

    @Async
    @TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
    public void handleClaimStatusChange(ClaimStatusChangedEvent event) {

        try {

            BurialClaim burialClaim = event.getClaim();
            Member member = burialClaim.getMember();

            String status = event.getClaimStatus().name();

            String subject = "Togetherness Women's Club - Burial Claim " + status;

            String body = "Dear " + member.getFirstName() + ",\n\n"
                    + "Your burial claim for " + burialClaim.getDeceasedName() + " has been " + status.toLowerCase() + ".\n\n"
                    + "Claim Details:\n"
                    + "- Deceased: " + burialClaim.getDeceasedName() + "\n"
                    + "- Amount: R" + burialClaim.getClaimAmount() + "\n"
                    + "- Status: " + status + "\n"
                    + "- Date: " + burialClaim.getClaimDate() + "\n\n"
                    + "If you have any questions, please contact the club treasurer.\n\n"
                    + "Togetherness Women's Club";

            emailService.sendEmail(member.getEmail(), subject, body);

            LOGGER.info("Burial Claim email has been sent successfully");

        } catch (Exception e) {
            LOGGER.error("Failed to send claim status email for claim {}: {}", event.getClaim().getId(), e.getMessage());
        }
    }
}
