package za.co.twc.togetherness.womens.club.exception;

public class DependentNotFoundException extends RuntimeException {
    public DependentNotFoundException(Long dependentId) {
        super("Dependent with id " + dependentId + " not found");
    }
}
