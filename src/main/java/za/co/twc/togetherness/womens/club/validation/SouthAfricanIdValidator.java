package za.co.twc.togetherness.womens.club.validation;

import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class SouthAfricanIdValidator implements ConstraintValidator<ValidSAId, String> {

    @Override
    public boolean isValid(String id, ConstraintValidatorContext context) {

        if (id == null || id.isBlank()) {
            return true; // handled by @NotBlank
        }

        if (!id.matches("\\d{13}")) {
            return false;
        }

        // Step 1: Sum odd digits
        int oddSum = 0;
        for (int i = 0; i < 12; i += 2) {
            oddSum += Character.getNumericValue(id.charAt(i));
        }

        // Step 2: Concatenate even digits
        int expectedCheckDigit = getCalculated(id, oddSum);

        int actualCheckDigit = Character.getNumericValue(id.charAt(12));

        return expectedCheckDigit == actualCheckDigit;
    }

    private static int getCalculated(String id, int oddSum) {
        StringBuilder evenDigits = new StringBuilder();
        for (int i = 1; i < 12; i += 2) {
            evenDigits.append(id.charAt(i));
        }

        int evenNumber = Integer.parseInt(evenDigits.toString());
        int doubled = evenNumber * 2;

        // Step 3: Sum digits of doubled
        int evenSum = 0;
        while (doubled > 0) {
            evenSum += doubled % 10;
            doubled /= 10;
        }

        // Step 4: Total
        int total = oddSum + evenSum;

        // Step 5: Calculate checksum
        return (10 - (total % 10)) % 10;
    }
}
