package hkhaung;

import java.lang.reflect.Array;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class QueryHandler {
    public static final Pattern PATTERN =
            Pattern.compile("^(SELECT) (.+?) FROM (.+?)(?: WHERE (.+?))?$",
                    Pattern.MULTILINE | Pattern.CASE_INSENSITIVE);


    public static Query parse(String queryString) {
        final Matcher matcher = PATTERN.matcher(queryString);
        if (matcher.find()) {
            String keyword = matcher.group(1);
            String columms = matcher.group(2);
            String table = matcher.group(3);
            String additional = matcher.group(4) != null ? matcher.group(4) : null;
            return new Query(keyword, columms, table, additional);
        }
        throw new IllegalStateException("invalid query: " + queryString);
    }

    // TODO: is hardcoded
    public static QueryResult<?> handle(byte[] fileBytes, String queryString) {
        Query query = parse(queryString);
        String keyword = query.getKeyword();
        String cols = query.getCol();
        String table = query.getTable();
        String additional = query.getAdditional();

        int pageSize = getPageSize(fileBytes);

        if (cols.toLowerCase().contains("count(*)")) {
            return countRowsQuery(fileBytes, table);
        }
        return null;
    }

    public static QueryResult<?> countRowsQuery(byte[] fileBytes, String table) {
        List<Integer> cellPointerArr = Utils.getCellPointerArrSqliteSchema(fileBytes, 100);  // 100 because we are on page 1 and we skip database header

        int rootPage = -1;
        for (int offset : cellPointerArr) {
            int recordSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
            int rowId = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});

            int recordHeaderSize = Utils.convertByteToInt(new byte[]{fileBytes[offset++]});
            int recordContentIndex = offset + recordHeaderSize - 1;
            byte[] serialTypes = Arrays.copyOfRange(fileBytes, offset, recordContentIndex);

            List<Integer> varints = VarintDecoder.decodeAllVarints(serialTypes);
            String prev = null;
            boolean twoQueryTablesFound = false;
            for (int varint : varints) {
                System.out.println("start");
                int contentSize = Utils.getContentSizeBySerialType(varint);
                byte[] contentBytes = Arrays.copyOfRange(fileBytes, recordContentIndex, recordContentIndex + contentSize);
                recordContentIndex += contentSize;
                String content = Utils.interpretAsString(contentBytes);
                System.out.println(content);
                System.out.println(Arrays.toString(contentBytes));
                System.out.println("end");
                if (Objects.equals(prev, table) && Objects.equals(content, table)) {
                    twoQueryTablesFound = true;  // Found two consecutive queryTable occurrences
                    continue;
                }

                if (twoQueryTablesFound) {
                    rootPage = Utils.convertByteToInt(contentBytes);
                    twoQueryTablesFound = false;
                }

                prev = content;
            }
        }

        int pageOffset = Utils.determinePageOffset(rootPage);
        int count = Utils.getCellPointerArrSqliteSchema(fileBytes, pageOffset).size();
        System.out.println(rootPage);
        System.out.println(pageOffset);
        return new QueryResult<>(List.of(count), true, null, 1);
    }
}
