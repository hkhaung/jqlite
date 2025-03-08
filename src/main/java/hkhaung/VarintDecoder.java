package hkhaung;
import java.util.ArrayList;
import java.util.List;


public class VarintDecoder {

    /* Decords a varint given a list of bytes and starting index */
    public static int decodeVarint(byte[] byteArray, int startIndex) {
        int result = 0;
        int currentIndex = startIndex;

        while (currentIndex < byteArray.length) {
            // Convert the byte to an unsigned int value (mask out the sign bit)
            int byteValue = byteArray[currentIndex] & 0xFF;

            // shift result left by 7 and then OR with 7 lower bits
            result = (result << 7) | (byteValue & 0x7F);

            // If the MSB is not set, we are done with this varint
            if ((byteValue & 0x80) == 0) {
                return result;
            }

            // Move to the next byte
            currentIndex++;
        }

        throw new IllegalArgumentException("Unexpected end of byte stream");
    }

    /*  */
    public static List<Integer> decodeAllVarints(byte[] byteArray) {
        List<Integer> varints = new ArrayList<>();
        int index = 0;
        while (index < byteArray.length) {
            int startIndex = index;
            int varint = decodeVarint(byteArray, index);
            varints.add(varint);

            // Move the index forward by counting the bytes consumed for this varint
            do {
                index++;
                // Break if we've reached the end of the array or found a byte with MSB not set
            } while (index < byteArray.length && (byteArray[index - 1] & 0x80) != 0);
        }
        return varints;
    }
}
