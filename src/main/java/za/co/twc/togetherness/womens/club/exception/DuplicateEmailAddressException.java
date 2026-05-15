package za.co.twc.togetherness.womens.club.exception;

public class DuplicateEmailAddressException extends RuntimeException {
    public DuplicateEmailAddressException(String email) {
        super("Duplicate email address: (" + email + ") already exists. Please use a different email address.");
    }
}
