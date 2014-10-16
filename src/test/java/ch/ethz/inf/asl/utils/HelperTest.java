package ch.ethz.inf.asl.utils;

import org.testng.annotations.Test;

import java.io.IOException;
import java.nio.ByteBuffer;
import java.util.LinkedList;
import java.util.List;

import static ch.ethz.inf.asl.testutils.TestConstants.SMALL;
import static org.testng.Assert.assertEquals;

public class HelperTest {

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testConcatenateWithNullArrays() {
            Helper.concatenate(null, null);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testConcatenateWithOneNullArray1() {
        Helper.concatenate(new byte[0], null);
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testConcatenateWithOneNullArray2() {
        Helper.concatenate(null, new byte[0]);
    }

    @Test(groups = SMALL)
    public void testConcatenateEmptyArrays() {
        byte[] result = Helper.concatenate(new byte[0], new byte[0]);
        assertEquals(result, new byte[0]);
    }

    @Test(groups = SMALL)
    public void testConcatenate() {
        byte[] ar1 = {1, 3, 4};
        byte[] ar2 = {9};

        byte[] result = Helper.concatenate(new byte[0], ar1);
        assertEquals(result, ar1);

        result = Helper.concatenate(ar2, new byte[0]);
        assertEquals(result, ar2);

        result = Helper.concatenate(ar2, ar1);
        assertEquals(result, new byte[] {9, 1, 3, 4});


        result = Helper.concatenate(ar1, ar2);
        assertEquals(result, new byte[] {1, 3, 4, 9});
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testSerializeWithNullObject() {
        try {
            Helper.serialize(null);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL)
    public void testSerializePutsTheLengthOfTheSerializedObjectInFront() {
        String aString = "This is a string to be used for a test! Sort life, isn't it?";
        try {
            byte[] serialized = Helper.serialize(aString);
            byte[] firstFourBytes = new byte[4];
            for (int i = 0; i < 4; ++i) {
                firstFourBytes[i] = serialized[i];
            }

            int length = ByteBuffer.wrap(firstFourBytes).getInt();
            assertEquals(serialized.length - 4, length);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testDeserializeNullByteArray() {
        try {
            Helper.deserialize(null);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*should contain more than 4 bytes.*")
    public void testDeserializeEmptyByteArray() {
        try {
            Helper.deserialize(new byte[0]);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL, expectedExceptions = IllegalArgumentException.class,
            expectedExceptionsMessageRegExp = ".*wasn't serialized.*")
    public void testDeserializeWithInvalidLength() {
        try {
            String someObject = "This is a string! Nice!";
            byte[] data = Helper.serialize(someObject);

            // change first byte
            data[0] = (byte) ~data[0];

            Helper.deserialize(data);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL)
    public void testDeserialize() {
        try {
            Class<String> someObject = String.class;

            byte[] serializedObject = Helper.serialize(someObject);
            Class<String> deserializedObject = (Class<String>) Helper.deserialize(serializedObject);

            assertEquals(deserializedObject, someObject);
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }
    }

    @Test(groups = SMALL, expectedExceptions = NullPointerException.class)
    public void testMakeIntegerListToPrimitiveIntArrayWithNullList() {
        Helper.makeIntegerListToPrimitiveIntArray(null);
    }

    @Test(groups = SMALL)
    public void testMakeIntegerListToPrimitiveIntArrayWithEmptyList() {
        int[] array = Helper.makeIntegerListToPrimitiveIntArray(new LinkedList<Integer>());
        assertEquals(array, new int[0]);
    }

    @Test(groups = SMALL)
    public void testMakeIntegerListToPrimitiveIntArray() {
        List<Integer> list = new LinkedList<>();
        list.add(13);
        list.add(29);
        list.add(178);

        int[] array = Helper.makeIntegerListToPrimitiveIntArray(list);
        assertEquals(array, new int[]{13, 29, 178});
    }
}
