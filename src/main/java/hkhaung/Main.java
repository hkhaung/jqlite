package hkhaung;


public class Main {
  public static void main(String[] args) {
    if (args.length < 2) {
      System.out.println("Missing <database path> and <command>");
      return;
    }

    String databaseFilePath = args[0];
    String command = args[1];

//    String databaseFilePath = "sample.db";
//    String command = ".tables";
//    String command = "SELECT COUNT(*) FROM apples";

    // dot commands
    switch (command) {
      case ".dbinfo" -> {  // print out page size and number of tables
        // get page size
        Handlers.dotDbInfoHandler(databaseFilePath);
      }

      case ".tables" -> {  // get names of tables
        Handlers.dotTablesHandler(databaseFilePath);
      }
    }

    // query
    // TODO: is hardcoded for now
    if (Utils.isSqlQuery(command)) {
      String[] query = command.split(" ");
      String type = query[0];
      String col = query[1];
      String table = query[query.length - 1];
      int numRows = Handlers.queryHandler(databaseFilePath, table);
      System.out.println(numRows);
    }
  }
}
