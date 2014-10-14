package ch.ethz.inf.asl.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;

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


    // taken from http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
    public static byte[] concat(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] serialize(Object object) throws IOException {
        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(object);

        byte[] objectData = bos.toByteArray();
        byte[] lengthOfObject = ByteBuffer.allocate(4).putInt(objectData.length).array();
        byte[] toSend = concat(lengthOfObject, objectData);

        return toSend;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }
}
