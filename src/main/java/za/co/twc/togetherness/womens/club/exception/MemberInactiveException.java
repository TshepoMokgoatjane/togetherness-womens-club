package za.co.twc.togetherness.womens.club.exception;

public class MemberInactiveException extends RuntimeException {
    public MemberInactiveException(Long memberId) {
        super("Inactive member with id " + memberId + " cannot be modified");
    }
}
