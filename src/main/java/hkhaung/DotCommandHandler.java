package hkhaung;

import java.util.Arrays;
import java.util.List;
import java.util.Objects;

import static hkhaung.Utils.readDbFile;


public class DotCommandHandler {
    public static void dotDbInfoHandler(byte[] fileBytes) {
        List<Integer> cellPointerArr = Utils.getCellPointerArrSqliteSchema(fileBytes, 100);  // 100 because we are on page 1 and we skip database header

        int pageSize = Utils.convertByteToInt(new byte[]{fileBytes[16], fileBytes[17]});
        System.out.println("database page size: " + pageSize);

        /* get table size
         * TODO - test cell pointers that lead to interior pages
         * */
        System.out.println("number of tables: " + cellPointerArr.size());
    }

    public static void dotTablesHandler(byte[] fileBytes) {
        // get all cells by looking at cell pointer arr of sqlite_schema page
        // read each cell
        List<Integer> cellPointerArr = Utils.getCellPointerArrSqliteSchema(fileBytes, 100);  // 100 because we are on page 1 and we skip database header

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
