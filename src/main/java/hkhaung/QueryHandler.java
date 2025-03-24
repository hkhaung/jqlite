package hkhaung;

import java.io.IOException;
import java.io.RandomAccessFile;
import java.lang.reflect.Array;
import java.util.ArrayList;
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
    public static QueryResult<?> handle(RandomAccessFile dbFile, String command) throws IOException {
        Query query = parse(command);
        String keyword = query.getKeyword();
        String cols = query.getCol();
        String tableName = query.getTable();
        String additional = query.getAdditional();

        int pageSize = Utils.readPageSize(dbFile);
        int tableRootPage = Utils.getTableRootPage(dbFile, pageSize, 1, tableName);

        if (cols.toLowerCase().contains("count(*)")) {
            int count = Utils.getNumRows(dbFile, pageSize, tableRootPage);
            return new QueryResult<>(List.of(count), true, null, 1);
        }
        return null;
    }
}
