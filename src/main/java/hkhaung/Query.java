package hkhaung;


public class Query {
    private String keyword;  // e.g., select, create, etc
    private String col;
    private String table;
    private String additional;

    // Constructor
    public Query(String keyword, String col, String table, String additional) {
        if (keyword == null || col == null || table == null) {
            throw new IllegalArgumentException("Keyword, column, and table cannot be null");
        }
        this.keyword = keyword;
        this.col = col;
        this.table = table;
        this.additional = additional != null ? additional : "";  // handle null for additional
    }

    // Getters
    public String getKeyword() {
        return keyword;
    }

    public String getCol() {
        return col;
    }

    public String getTable() {
        return table;
    }

    public String getAdditional() {
        return additional;
    }

    // Setters
    public void setKeyword(String keyword) {
        this.keyword = keyword;
    }

    public void setCol(String col) {
        this.col = col;
    }

    public void setTable(String table) {
        this.table = table;
    }

    public void setAdditional(String additional) {
        this.additional = additional;
    }

    @Override
    public String toString() {
        return "Query{" + "keyword='" + keyword + '\'' + ", col='" + col + '\'' + ", table='" + table + '\'' +
                ", additional='" + additional + '\'' + '}';
    }
}
