package za.co.twc.togetherness.womens.club.exception;

public class MemberHasDependentsException extends RuntimeException {
    public MemberHasDependentsException(Long memberId) {
        super("Can't delete member with id " + memberId + " because dependents exist!");
    }
}