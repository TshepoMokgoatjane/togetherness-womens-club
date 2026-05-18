package za.co.twc.togetherness.womens.club.scheduler;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import za.co.twc.togetherness.womens.club.domain.Contribution;
import za.co.twc.togetherness.womens.club.domain.ContributionStatus;
import za.co.twc.togetherness.womens.club.repository.ContributionRepository;
import za.co.twc.togetherness.womens.club.service.ContributionService;

import java.time.YearMonth;
import java.util.List;

@Component
public class ContributionScheduler {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContributionScheduler.class);

    private final ContributionService contributionService;

    @Autowired
    public ContributionScheduler(ContributionService contributionService) {
        this.contributionService = contributionService;
    }

    // Runs on the 1st day of every month at midnight
    //@Scheduled(cron = "0 0 0 1 * ?")

    // For TESTING PURPOSES - Change cron to run every minute:
    @Scheduled(cron = "0 */1 * * * ?")
    public void generateMonthlyContributions() {
        LOGGER.info("Generating monthly contributions for this month.");
        contributionService.generateMonthlyContributions();
    }

    @Scheduled(cron = "0 0 23 L * ?") // last day of the month
    public void markMissedContributions() {
        contributionService.markMissedContributions();
    }
}
