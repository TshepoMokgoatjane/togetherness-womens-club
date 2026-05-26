package za.co.twc.togetherness.womens.club.exception;

public class InvalidEmailTokenException extends RuntimeException {
    public InvalidEmailTokenException(String token) {

        super("Invalid token " + token);
    }
}
