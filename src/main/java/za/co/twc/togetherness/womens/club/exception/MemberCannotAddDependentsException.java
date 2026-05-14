package za.co.twc.togetherness.womens.club.exception;

public class MemberCannotAddDependentsException extends RuntimeException {
    public MemberCannotAddDependentsException(Long id) {
        super("Only ACTIVE members can add dependents to member with id " + id);
    }
}
