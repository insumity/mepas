package ch.ethz.inf.asl.utils;

import java.io.*;
import java.nio.ByteBuffer;
import java.util.Arrays;
import java.util.List;

import static ch.ethz.inf.asl.utils.Verifier.notNull;
import static ch.ethz.inf.asl.utils.Verifier.verifyTrue;

public class Helper {

    // taken from http://stackoverflow.com/questions/80476/how-to-concatenate-two-arrays-in-java
    public static byte[] concatenate(byte[] first, byte[] second) {
        byte[] result = Arrays.copyOf(first, first.length + second.length);
        System.arraycopy(second, 0, result, first.length, second.length);
        return result;
    }

    public static byte[] serialize(Object object) throws IOException {
        notNull(object, "Given object cannot be null!");

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ObjectOutput out = new ObjectOutputStream(bos);
        out.writeObject(object);

        byte[] objectData = bos.toByteArray();
        byte[] lengthOfObject = ByteBuffer.allocate(4).putInt(objectData.length).array();
        byte[] toSend = concatenate(lengthOfObject, objectData);

        return toSend;
    }

    public static Object deserialize(byte[] data) throws IOException, ClassNotFoundException {
        notNull(data, "Given data array cannot be null!");
        verifyTrue(data.length >= 4, "Given data array should contain more than 4 bytes (1 int)!");

        // verify serialized object has correct length, i.e. the length in front of the data array is valid
        byte[] firstFourBytes = new byte[4];
        for (int i = 0; i < 4; ++i) {
            firstFourBytes[i] = data[i];
        }
        int length = ByteBuffer.wrap(firstFourBytes).getInt();
        if (length != data.length - 4) {
            throw new IllegalArgumentException("Given data array wasn't serialized using the serialize method!");
        }

        // deserialize remaining part
        data = Arrays.copyOfRange(data, 4, data.length);
        ByteArrayInputStream in = new ByteArrayInputStream(data);
        ObjectInputStream is = new ObjectInputStream(in);
        return is.readObject();
    }

    public static int[] makeIntegerListToPrimitiveIntArray(List<Integer> list) {
        int[] array = new int[list.size()];
        int index = 0;
        for (Integer i: list) {
            array[index] = i;
            index++;
        }

        return array;
    }
}
