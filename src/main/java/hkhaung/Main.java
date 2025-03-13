package hkhaung;


import java.io.IOException;
import java.io.RandomAccessFile;


public class Main {
  public static void main(String[] args) throws IOException {
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }

    String databaseFilePath = args[0];
    String command = args[1];

//    String databaseFilePath = "sample.db";
//    String command = ".tables";
//    String command = "SELECT COUNT(*) FROM apples";

    RandomAccessFile dbFile = Utils.readDbFile(databaseFilePath);

    // dot commands
    switch (command) {
      case ".dbinfo" -> {  // print out page size and number of tables
        // get page size
        DotCommandHandler.dotDbInfoHandler(dbFile);
      }

      case ".tables" -> {  // get names of tables
        DotCommandHandler.dotTablesHandler(dbFile);
      }
    }

    // query
    if (Utils.isSqlQuery(command)) {
      QueryResult<?> result = QueryHandler.handle(dbFile, command);
      if (result != null) {
        for (Object ele : result) {
          System.out.println(ele.toString());
        }
      }
    }
  }
}
