package za.co.twc.togetherness.womens.club.utilities;

import jakarta.servlet.http.HttpServletRequest;

public class RequestUtils {

    public static String getClientIP(HttpServletRequest request) {
        String xfHeader = request.getHeader("X-Forwarded-For");

        if (xfHeader == null || xfHeader.isEmpty()) {
            return request.getRemoteAddr();
        }

        return xfHeader.split(",")[0];
    }
}
