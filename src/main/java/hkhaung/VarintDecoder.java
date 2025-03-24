package hkhaung;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.ArrayList;
import java.util.List;


public class VarintDecoder {

    public static int decodeVarint(RandomAccessFile dbFile) throws IOException {
        int result = 0;
        while (true) {
            // read 8 bits
            int byteValue = dbFile.readByte();

            // get the lower 7 bits of current byte
            // shift result to left by 7 bits to make space for the lower 7 bits
            result = (result << 7) | (byteValue & 0b01111111);

            // stop if we have a 0 as most significant bit
            if ((byteValue & 0b10000000) == 0) {
                return result;
            }
        }
    }
}
