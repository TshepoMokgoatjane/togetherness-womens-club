package za.co.twc.togetherness.womens.club.exception;

import za.co.twc.togetherness.womens.club.domain.Contribution;

public class DuplicateMonthlyContributionException extends RuntimeException {
    public DuplicateMonthlyContributionException(Contribution contribution) {

        super("Contribution of amount " + contribution.getAmount() + " already recorded for member: " + contribution.getMember().getMemberNumber() + " for this month " + contribution.getContributionMonth());
    }
}
