package za.co.twc.togetherness.womens.club.exception;

public class FailedToSendPasswordResetEmailException extends RuntimeException {
    public FailedToSendPasswordResetEmailException(String message) {
        super("Unable to send password reset email. Please try again later. " + message);
    }
}
