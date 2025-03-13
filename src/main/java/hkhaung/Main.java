package hkhaung;


import java.io.IOException;


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

    byte[] fileBytes = Utils.readDbFile(databaseFilePath);

    // dot commands
    switch (command) {
      case ".dbinfo" -> {  // print out page size and number of tables
        // get page size
        DotCommandHandler.dotDbInfoHandler(fileBytes);
      }

      case ".tables" -> {  // get names of tables
        DotCommandHandler.dotTablesHandler(fileBytes);
      }
    }

    // query
    if (Utils.isSqlQuery(command)) {
      QueryResult<?> result = QueryHandler.handle(fileBytes, command);
      if (result != null) {
        for (Object ele : result) {
          System.out.println(ele.toString());
        }
      }
    }
  }
}
