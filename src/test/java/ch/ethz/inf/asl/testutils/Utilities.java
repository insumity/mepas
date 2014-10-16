package ch.ethz.inf.asl.testutils;

public class Utilities {

     /**
     * Creates a string of the given length containing the given character.
     * @param length the length of the to string to be created
     * @param c the only character that the string is going to contain
     * @return generated string
     */
    public static String createStringWith(int length, char c) {
        StringBuffer stringBuffer = new StringBuffer();

        for (int i = 0; i < length; ++i) {
            stringBuffer.append(c);
        }

        return stringBuffer.toString();
    }
}
