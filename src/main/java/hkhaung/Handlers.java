package hkhaung;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;


public class Handlers {

    public static byte[] readDbFile(String databaseFilePath) {
        try {
            return Files.readAllBytes(Paths.get(databaseFilePath));
        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
        return null;
    }

    public static void dotDbInfoHandler(String databaseFilePath) {
        byte[] fileBytes = readDbFile(databaseFilePath);
        if (fileBytes == null) {
            return;
        }
        List<Integer> cellPointerArr = Utils.getCellPointerArrSqliteSchema(fileBytes);

        int pageSize = Utils.convertByteToInt(new byte[]{fileBytes[16], fileBytes[17]});
        System.out.println("database page size: " + pageSize);

        /* get table size
         * TODO - test cell pointers that lead to interior pages
         * */
        System.out.println("number of tables: " + cellPointerArr.size());
    }

    public static void dotTablesHandler(String databaseFilePath) {
        // get all cells by looking at cell pointer arr of sqlite_schema page
        // read each cell
        byte[] fileBytes = readDbFile(databaseFilePath);
        if (fileBytes == null) {
            return;
        }
        List<Integer> cellPointerArr = Utils.getCellPointerArrSqliteSchema(fileBytes);

        StringBuilder tableNames = new StringBuilder();
        for (int offset : cellPointerArr) {
            int recordSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
            int rowId = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});

            int recordHeaderSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
            int recordContentIndex = offset + recordHeaderSize - 1;
            byte[] serialTypes = Arrays.copyOfRange(fileBytes, offset, recordContentIndex);
            List<Integer> varints = VarintDecoder.decodeAllVarints(serialTypes);
            String prev = null;
            for (int varint : varints) {
                int contentSize = Utils.getContentSizeBySerialType(varint);
                byte[] contentBytes = Arrays.copyOfRange(fileBytes, recordContentIndex, recordContentIndex + contentSize);
                recordContentIndex += contentSize;
                String content = Utils.interpretAsString(contentBytes);
                if (Objects.equals(prev, "table") && !Objects.equals(content, "sqlite_sequence") && content != null) {
                    tableNames.append(content);
                    tableNames.append(" ");
                }
                prev = content;
            }
        }

        System.out.println(tableNames.toString().trim());
    }
}
