package za.co.twc.togetherness.womens.club.exception;

public class DuplicateDependentException extends RuntimeException {
    public DuplicateDependentException(String idNumber) {
        super("A dependent with ID number " + idNumber + " already exists.");
    }
}
