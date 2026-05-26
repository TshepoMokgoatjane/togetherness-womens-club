package za.co.twc.togetherness.womens.club.exception;

public class EmailTokenExpiredException extends RuntimeException {
    public EmailTokenExpiredException(String token) {
        super("Token expired! " + token);
    }
}
