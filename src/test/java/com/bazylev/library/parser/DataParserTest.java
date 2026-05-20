package com.bazylev.library.parser;

import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Visitor;
import com.bazylev.library.exception.DataParseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class DataParserTest {

  private DataParser parser;

  @TempDir
  Path tempDir;

  @BeforeEach
  void setUp() throws Exception {
    parser = new DataParser();
    Field instanceField = Library.class.getDeclaredField("INSTANCE");
    instanceField.setAccessible(true);
    ((AtomicReference<?>) instanceField.get(null)).set(null);
  }

  @Test
  void parseLibrary_loadsBooksFromFile() throws IOException, DataParseException {
    Path file = tempDir.resolve("library.txt");
    Files.writeString(file, "1984, George Orwell\nDune, Frank Herbert\n");

    Library library = parser.parseLibrary(file.toString());

    assertEquals(2, library.getAvailableBooksCount());
  }

  @Test
  void parseLibrary_skipsBlankLines() throws IOException, DataParseException {
    Path file = tempDir.resolve("library.txt");
    Files.writeString(file, "1984, George Orwell\n\nDune, Frank Herbert\n");

    Library library = parser.parseLibrary(file.toString());

    assertEquals(2, library.getAvailableBooksCount());
  }

  @Test
  void parseLibrary_throwsDataParseExceptionForMissingFile() {
    assertThrows(DataParseException.class,
        () -> parser.parseLibrary("nonexistent/path/file.txt"));
  }

  @Test
  void parseVisitors_loadsVisitorsFromFile() throws IOException, DataParseException {
    Path file = tempDir.resolve("visitors.txt");
    Files.writeString(file, "Alice, 2\nBob, 3\n");

    List<Visitor> visitors = parser.parseVisitors(file.toString());

    assertEquals(2, visitors.size());
  }

  @Test
  void parseVisitors_setsNameAndLimitCorrectly() throws IOException, DataParseException {
    Path file = tempDir.resolve("visitors.txt");
    Files.writeString(file, "Alice, 2\n");

    List<Visitor> visitors = parser.parseVisitors(file.toString());

    assertEquals("Alice", visitors.getFirst().getName());
    assertEquals(2, visitors.getFirst().getBookLimit());
  }

  @Test
  void parseVisitors_skipsBlankLines() throws IOException, DataParseException {
    Path file = tempDir.resolve("visitors.txt");
    Files.writeString(file, "Alice, 2\n\nBob, 3\n");

    List<Visitor> visitors = parser.parseVisitors(file.toString());

    assertEquals(2, visitors.size());
  }

  @Test
  void parseVisitors_throwsDataParseExceptionForMissingFile() {
    assertThrows(DataParseException.class,
        () -> parser.parseVisitors("nonexistent/path/file.txt"));
  }
}
