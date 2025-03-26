package hkhaung;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.nio.ByteBuffer;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

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

        return -1;
    }

    public static int getNumRows(RandomAccessFile dbFile, int pageSize, int pageNum) throws IOException {
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

        if (bTreePageType == interiorTablePage) {
            // TODO
        } else if (bTreePageType == leafTablePage) {
            return numCells;
        }

        return -1;
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
            for (int i = 0; i < numCells; i++) {
                short cellPointer = cellPointerArr[i];
                dbFile.seek(start + cellPointer);

                int recordSize = VarintDecoder.decodeVarint(dbFile);
                int rowId = VarintDecoder.decodeVarint(dbFile);
                int recordHeaderSize = VarintDecoder.decodeVarint(dbFile);

                long stop = dbFile.getFilePointer() + (recordHeaderSize - 1);
                List<Integer> varints = new ArrayList<>();
                while (dbFile.getFilePointer() < stop) {
                    int varint = VarintDecoder.decodeVarint(dbFile);
                    varints.add(varint);
                }

                String prevContentValue = null;
                for (int varint: varints) {
                    int contentSize = getContentSizeBySerialType(varint);
                    byte[] contentBytes = new byte[contentSize];
                    int bytesRead = dbFile.read(contentBytes);
                    if (bytesRead == -1) {
                        throw new IOException("End of file reached.");
                    }

                    String contentValue = interpretAsString(contentBytes);
                    if ("table".equals(prevContentValue) && !"sqlite_sequence".equals(contentValue)) {
                        tableNames.append(contentValue).append(" ");
                        break;
                    }
                    prevContentValue = contentValue;

                }
            }
            return tableNames.toString().trim();
        }


        throw new IOException("getTableNames error");
    }

    public static int getTableRootPage(RandomAccessFile dbFile, int pageSize, int pageNum, String tableName) throws IOException {
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

            for (int i = 0; i < numCells; i++) {
                short cellPointer = cellPointerArr[i];
                dbFile.seek(start + cellPointer);

                int recordSize = VarintDecoder.decodeVarint(dbFile);
                int rowId = VarintDecoder.decodeVarint(dbFile);
                int recordHeaderSize = VarintDecoder.decodeVarint(dbFile);

                long stop = dbFile.getFilePointer() + (recordHeaderSize - 1);
                List<Integer> varints = new ArrayList<>();
                while (dbFile.getFilePointer() < stop) {
                    int varint = VarintDecoder.decodeVarint(dbFile);
                    varints.add(varint);
                }

                List<byte[]> content = new ArrayList<>();
                for (int varint: varints) {
                    int contentSize = getContentSizeBySerialType(varint);
                    byte[] contentBytes = new byte[contentSize];
                    int bytesRead = dbFile.read(contentBytes);
                    if (bytesRead == -1) {
                        throw new IOException("End of file reached.");
                    }
                    content.add(contentBytes);
                }

                if (content.size() > 2) {
                    String currentTableName = new String(content.get(2));
                    if (currentTableName.equals(tableName)) {
                        byte[] rootPageNumber = content.get(3);
                        return byteArrayToInt(rootPageNumber);
                    }
                }

            }
            return -1;
        }


        throw new IOException("getTableRootPage error");
    }

    public static List<String> getColNamesOfTable(RandomAccessFile dbFile, int pageSize, int pageNum, String tableName) throws IOException {
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
            for (int i = 0; i < numCells; i++) {
                short cellPointer = cellPointerArr[i];
                dbFile.seek(start + cellPointer);

                int recordSize = VarintDecoder.decodeVarint(dbFile);
                int rowId = VarintDecoder.decodeVarint(dbFile);
                int recordHeaderSize = VarintDecoder.decodeVarint(dbFile);

                long stop = dbFile.getFilePointer() + (recordHeaderSize - 1);
                List<Integer> varints = new ArrayList<>();
                while (dbFile.getFilePointer() < stop) {
                    int varint = VarintDecoder.decodeVarint(dbFile);
                    varints.add(varint);
                }

                List<byte[]> content = new ArrayList<>();
                for (int varint: varints) {
                    int contentSize = getContentSizeBySerialType(varint);
                    byte[] contentBytes = new byte[contentSize];
                    int bytesRead = dbFile.read(contentBytes);
                    if (bytesRead == -1) {
                        throw new IOException("End of file reached.");
                    }
                    content.add(contentBytes);
                }

                if (content.size() > 2) {
                    String currentTableName = new String(content.get(2));
                    if (currentTableName.equals(tableName)) {
                        String createTableStatement = new String(content.get(4));
                        List<Column> columnsNamesTypes = extractColNamesTypes(createTableStatement);
                        List<String> colNames = new ArrayList<>();
                        for (Column col: columnsNamesTypes) {
                            colNames.add(col.getName());
                        }
                        return colNames;
                    }
                }
            }
        }

        throw new IOException("getTableNames error");
    }

    public static String getColVals(RandomAccessFile dbFile, int pageSize, int pageNum, String tableName, String colName) throws IOException {
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
            List<String> colNames = getColNamesOfTable(dbFile, pageSize, 1, tableName);
            return "";
        }
//            int colNamesIndex = 0;
//
//            short[] cellPointerArr = new short[numCells];
//            for (int i = 0; i < numCells; i++) {
//                cellPointerArr[i] = dbFile.readShort();
//            }
//
//            StringBuilder colVals = new StringBuilder();
//            for (int i = 0; i < numCells; i++) {
//                short cellPointer = cellPointerArr[i];
//                dbFile.seek(start + cellPointer);
//
//                int recordSize = VarintDecoder.decodeVarint(dbFile);
//                int rowId = VarintDecoder.decodeVarint(dbFile);
//                int recordHeaderSize = VarintDecoder.decodeVarint(dbFile);
//
//                long stop = dbFile.getFilePointer() + (recordHeaderSize - 1);
//                List<Integer> varints = new ArrayList<>();
//                while (dbFile.getFilePointer() < stop) {
//                    int varint = VarintDecoder.decodeVarint(dbFile);
//                    varints.add(varint);
//                }
//
//                for (int varint: varints) {
//                    int contentSize = getContentSizeBySerialType(varint);
//                    byte[] contentBytes = new byte[contentSize];
//                    int bytesRead = dbFile.read(contentBytes);
//                    if (bytesRead == -1) {
//                        throw new IOException("End of file reached.");
//                    }
//
//                    if (colNames.get(colNamesIndex % colNames.size()).equals(colName)) {
//                        String contentValue = interpretAsString(contentBytes);
//                        colVals.append(contentValue).append('\n');
//                    }
//                    colNamesIndex++;
//
//                }
//            }
//            return colVals.toString().trim();
//        }
//
        throw new IOException("getColVals error");

    }


    /* get content size for serial type of record */
    private static int getContentSizeBySerialType(int num) {
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
    private static String interpretAsString(byte[] bytes) {
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

    private static int byteArrayToInt(byte[] bytes) {
        if (bytes.length > 4) {
            throw new IllegalArgumentException("Byte array length > 4");
        }

        byte[] paddedBytes = new byte[4];
        System.arraycopy(bytes, 0, paddedBytes, 4 - bytes.length, bytes.length);
        ByteBuffer buffer = ByteBuffer.wrap(paddedBytes);
        return buffer.getInt();
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
}
