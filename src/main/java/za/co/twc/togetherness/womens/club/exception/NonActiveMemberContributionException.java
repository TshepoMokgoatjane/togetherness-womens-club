package za.co.twc.togetherness.womens.club.exception;

public class NonActiveMemberContributionException extends RuntimeException {
    public NonActiveMemberContributionException(Long memberId) {
        super("Only ACTIVE members can make contributions. Member " + memberId +" is INACTIVE.");
    }
}
