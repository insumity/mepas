package ch.ethz.inf.asl.utils;

public class Verifier {

    /**
     * Throws a NullPointerException if the given object is null.
     * @param obj given object to be checked if it is null
     * @param message message of the exception thrown
     */
    public static void notNull(Object obj, String message) {
        if (message == null) {
            throw new NullPointerException("Given message cannot be null!");
        }

        if (obj == null) {
               throw new NullPointerException(message);
        }
    }

    public static void hasText(String string, String message) {
        notNull(string, message);
        notNull(message, "Given message cannot be null!");

        if (string.isEmpty()) {
            throw new IllegalArgumentException(message);
        }
    }

    public static void verifyTrue(boolean value, String message) {
        notNull(message, "Given message cannot be null!");
        if (value != true) {
            throw new IllegalArgumentException(message);
        }
    }

}
