package hkhaung;

import java.io.IOException;
import java.io.RandomAccessFile;


public class DotCommandHandler {
    public static void dotDbInfoHandler(RandomAccessFile dbFile) throws IOException {
        int pageSize = Utils.readPageSize(dbFile);
        System.out.println("database page size: " + pageSize);

        int numTables = Utils.getNumTables(dbFile, pageSize, 1);
        System.out.println("number of tables: " + numTables);
    }

    public static void dotTablesHandler(RandomAccessFile dbFile) throws IOException {
        int pageSize = Utils.readPageSize(dbFile);
        String tableNames = Utils.getTableNames(dbFile, pageSize, 1);
        System.out.println(tableNames);
    }
}
