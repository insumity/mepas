package ch.ethz.inf.asl.utils;

public class Helper {

    /**
     * Throws a NullPointerException if the given object is null.
     * @param obj given object to be checked if it is null
     * @param message message of the exception thrown
     */
    public static void notNull(Object obj, String message) {
        if (obj == null) {
            throw new NullPointerException(message);
        }
    }

    public static void hasText(String obj, String message) {
        notNull(obj, message);
        if (obj.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void verifyTrue(boolean value, String message) {
        if (value != true) {
            throw new IllegalArgumentException(message);
        }
    }
}
