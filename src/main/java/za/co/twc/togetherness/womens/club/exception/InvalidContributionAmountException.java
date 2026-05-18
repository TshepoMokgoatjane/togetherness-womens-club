package za.co.twc.togetherness.womens.club.exception;

import za.co.twc.togetherness.womens.club.domain.Contribution;

public class InvalidContributionAmountException extends RuntimeException {
    public InvalidContributionAmountException(Contribution contribution) {
        super("Invalid contribution amount " + contribution.getAmount() + " for member: " + contribution.getMember().getMemberNumber() + ". Contribution amount must be greater than 0.");
    }
}
