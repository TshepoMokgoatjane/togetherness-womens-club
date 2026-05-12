package za.co.twc.togetherness.womens.club.exception;

public class MemberNotFoundException extends RuntimeException {

    public MemberNotFoundException() {}

    public MemberNotFoundException(Long id) {
        super("Member with id " + id + " not found");
    }
}
