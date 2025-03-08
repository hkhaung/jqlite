package hkhaung;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;


public class JQLiteTests {
//    private static final String DBFILE = "northwind.db";
    private static final String DBFILE = "sample.db";

    private ByteArrayOutputStream outputStream;
    private final PrintStream originalSystemOut = System.out;

    @BeforeEach
    void setUp() {
        // Set up the output stream redirection before each test
        outputStream = new ByteArrayOutputStream();
        PrintStream printStream = new PrintStream(outputStream);
        System.setOut(printStream);  // Redirect System.out to the custom PrintStream
    }

    @AfterEach
    void tearDown() {
        // Reset System.out back to its original state after each test
        System.setOut(originalSystemOut);
    }

    @Test
    void testDbInfo() {
        Main.main(new String[]{DBFILE, ".dbinfo"});
        String expectedOutput = "database page size: 4096" + System.lineSeparator() + "number of tables: 3" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void testNamesTables() {
        Main.main(new String[]{DBFILE, ".tables"});
        String expectedOutput = "apples oranges" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }

    @Test
    void testCountRowsQuery() {
        Main.main(new String[]{DBFILE, "SELECT COUNT(*) FROM apples"});
        String expectedOutput = "4" + System.lineSeparator();
        assertEquals(expectedOutput, outputStream.toString());
    }
}
