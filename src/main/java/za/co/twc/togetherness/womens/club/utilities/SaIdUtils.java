package za.co.twc.togetherness.womens.club.utilities;

import java.time.LocalDate;

public class SaIdUtils {

    public static LocalDate extractDobFromId(String idNumber) {

        String yy = idNumber.substring(0, 2);
        String mm = idNumber.substring(2, 4);
        String dd = idNumber.substring(4, 6);

        int year = Integer.parseInt(yy);

        int currentYear = LocalDate.now().getYear() % 100;

        if (year > currentYear) {
            year += 1900;
        } else {
            year += 2000;
        }

        return LocalDate.of(year, Integer.parseInt(mm), Integer.parseInt(dd));
    }
}
