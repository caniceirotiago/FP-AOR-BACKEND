package aor.fpbackend.utils;
/**
 * GlobalSettings holds the application-wide constants used for configuration and default values.
 * <br>
 * These settings include default URLs, expiration times, timeout durations, and other constants
 * that are used throughout the application to ensure consistent behavior and configuration.
 * <br>
 */
public class GlobalSettings {
    public static final String USER_DEFAULT_PHOTO_URL = "https://cdn.pixabay.com/photo/2015/03/04/22/35/avatar-659651_640.png";
    public static final int CONFIRMATION_TOKEN_EXPIRATION_TIME_H = 24;
    public static final int PASSWORD_RESET_PREVENT_TIMER_MIN = 30;
    public static final int CONFIRMATION_EMAIL_PREVENT_TIMER_MIN = 1;
    public static final int DEFAULT_SESSION_TIMEOUT_MILLIS = 36000000;
    public static final int DEFAULT_NUMBER_MEMBERS_PER_PROJECT = 4;
    public static final int TIME_OUT_RATIO = 2; // denominator of the ratio for renovating the session timeout
}
