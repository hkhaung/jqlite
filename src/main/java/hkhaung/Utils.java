package hkhaung;
import java.io.IOException;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    public static byte[] readDbFile(String databaseFilePath) throws IOException {
        try {
            return Files.readAllBytes(Paths.get(databaseFilePath));
        } catch (IOException e) {
            throw new IOException("Error reading file: " + e.getMessage());
        }
    }

    /* Returns an integer value of given bytes arr */
    public static int convertByteToInt(byte[] bytes) {
        if (bytes == null) {
            throw new IllegalArgumentException("bytes arg missing");
        }
        if (bytes.length == 1) {
            return Byte.toUnsignedInt(bytes[0]);
        }

        short signed = ByteBuffer.wrap(bytes).getShort();
        return Short.toUnsignedInt(signed);
    }

    /* Given a file (.db) in bytes, returns the cell pointer array from
    * sqlite_schema page  */
    public static List<Integer> getCellPointerArrSqliteSchema(byte[] fileBytes, int cellPointerArrIndex) {
        cellPointerArrIndex += 8;  // skip the b-tree page header
        List<Integer> cellPointerArr = new ArrayList<>();
        for (int i = cellPointerArrIndex; i < fileBytes.length - 1; i += 2) {
            if (fileBytes[i] == 0 && fileBytes[i + 1] == 0) {
                break;
            }
            int offset = Utils.convertByteToInt(new byte[]{fileBytes[i], fileBytes[i + 1]});
            cellPointerArr.add(offset);
        }
        return cellPointerArr;
    }

    /* get content size for serial type of record */
    public static int getContentSizeBySerialType(int num) {
        if (num >= 12) {
            if (num % 2 == 0) {
                return (num - 12) / 2;
            } else {
                return (num - 13) / 2;
            }
        }

        return switch (num) {
            case 0, 8, 9 -> 0;
            case 5 -> 6;
            case 6, 7 -> 8;
            default -> num;
        };
    }

    /* try to convert string of bytes to string */
    public static String interpretAsString(byte[] bytes) {
        try {
            String decodedString = new String(bytes, StandardCharsets.US_ASCII);
            if (decodedString.matches("[\\x00-\\x7E]+")) {
                return decodedString;
            } else {
                return null;
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean isSqlQuery(String input) {
        if (input == null || input.trim().isEmpty()) {
            return false;
        }

        // Convert to uppercase for case-insensitive comparison
        String command = input.trim().toUpperCase();

        // Basic check: Look for common SQL commands (like SELECT, INSERT, etc.)
        return command.startsWith("SELECT") || command.startsWith("INSERT") ||
                command.startsWith("UPDATE") || command.startsWith("DELETE") ||
                command.startsWith("CREATE") || command.startsWith("DROP") ||
                command.startsWith("ALTER");
    }

    /* calculate page offset given pageNum
    * pageNum is rootpage */
    public static int determinePageOffset(int pageNum) {
        int pageSize = 4096;
        return (pageNum - 1) * pageSize;
    }
}
