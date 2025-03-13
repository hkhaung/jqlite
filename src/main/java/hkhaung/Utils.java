package hkhaung;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

public class Utils {
    static final byte interiorIndexPage = 0x02;
    static final byte interiorTablePage = 0x05;
    static final byte leafIndexPage = 0x0a;
    static final byte leafTablePage = 0x0d;

    public static RandomAccessFile readDbFile(String databaseFilePath) throws IOException {
        try {
            return new RandomAccessFile(databaseFilePath, "r");
        } catch (IOException e) {
            throw new IOException("Error reading file: " + e.getMessage());
        }
    }

    /* Read page size from .db file. The page size is the 16th and 17th byte of the .db file. */
    public static int readPageSize(RandomAccessFile dbFile) throws IOException {
        dbFile.seek(16);
        return Short.toUnsignedInt(dbFile.readShort());
    }

    public static int getNumTables(RandomAccessFile dbFile, int pageSize, int pageNum) throws IOException {
        int start = (pageNum - 1) * pageSize;
        dbFile.seek(start);
        if (pageNum == 1) {
            dbFile.skipBytes(100);  // skip database header
        }

        // read b tree page header (8 bytes)
        byte bTreePageType = dbFile.readByte();
        dbFile.skipBytes(2);
        int numCells = Short.toUnsignedInt(dbFile.readShort());
        dbFile.skipBytes(3);

        if (bTreePageType == interiorTablePage) {
            // TODO
        } else if (bTreePageType == leafTablePage) {
            return numCells;
        }
    }

    public static String getTableNames(RandomAccessFile dbFile, int pageSize, int pageNum) throws IOException {
        // get all cells by looking at cell pointer arr of sqlite_schema page
        // read each cell
        int start = (pageNum - 1) * pageSize;
        dbFile.seek(start);
        if (pageNum == 1) {
            dbFile.skipBytes(100);  // skip database header
        }

        // read b tree page header (8 bytes)
        byte bTreePageType = dbFile.readByte();
        dbFile.skipBytes(2);
        int numCells = Short.toUnsignedInt(dbFile.readShort());
        dbFile.skipBytes(3);

        if (bTreePageType == interiorTablePage) {
            // TODO
        } else if (bTreePageType == leafTablePage) {
            short[] cellPointerArr = new short[numCells];
            for (int i = 0; i < numCells; i++) {
                cellPointerArr[i] = dbFile.readShort();
            }

            StringBuilder tableNames = new StringBuilder();
            for (short offset : cellPointerArr) {
                dbFile.seek(start + offset);

                int recordSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
                int rowId = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});

//                int recordHeaderSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
//                int recordContentIndex = offset + recordHeaderSize - 1;
//                byte[] serialTypes = Arrays.copyOfRange(fileBytes, offset, recordContentIndex);
//
//                List<Integer> varints = VarintDecoder.decodeAllVarints(serialTypes);
//                String prev = null;
//                for (int varint : varints) {
//                    int contentSize = Utils.getContentSizeBySerialType(varint);
//                    byte[] contentBytes = Arrays.copyOfRange(fileBytes, recordContentIndex, recordContentIndex + contentSize);
//                    recordContentIndex += contentSize;
//                    String content = Utils.interpretAsString(contentBytes);
//                    if (Objects.equals(prev, "table") && !Objects.equals(content, "sqlite_sequence") && content != null) {
//                        tableNames.append(content);
//                        tableNames.append(" ");
//                    }
//                    prev = content;
//                }
            }
            return tableNames.toString().trim();
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
