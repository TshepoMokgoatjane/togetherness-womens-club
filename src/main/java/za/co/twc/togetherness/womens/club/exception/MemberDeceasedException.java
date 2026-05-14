package za.co.twc.togetherness.womens.club.exception;

public class MemberDeceasedException extends RuntimeException {
    public MemberDeceasedException(Long memberId) {
        super("Cannot delete a deceased member with id " + memberId);
    }
}
