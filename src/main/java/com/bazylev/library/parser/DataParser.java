package com.bazylev.library.parser;

import com.bazylev.library.entity.Book;
import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Reader;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class DataParser {

  private static final Logger logger = LogManager.getLogger(DataParser.class);

  private static final String FIELD_SEPARATOR = ",";
  private static final int BOOK_TITLE_INDEX = 0;
  private static final int BOOK_AUTHOR_INDEX = 1;
  private static final int READER_NAME_INDEX = 0;
  private static final int READER_LIMIT_INDEX = 1;

  public Library parseLibrary(String filePath) {
    Library library = Library.getInstance();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] parts = line.split(FIELD_SEPARATOR);
        String title = parts[BOOK_TITLE_INDEX].strip();
        String author = parts[BOOK_AUTHOR_INDEX].strip();
        library.addBook(new Book(title, author));
      }
    } catch (IOException e) {
      logger.error("Failed to read library data from file: {}", filePath, e);
      throw new RuntimeException("Cannot load library data", e);
    }
    logger.info("Library loaded with {} books", library.getAvailableBooksCount());
    return library;
  }

  public List<Reader> parseReaders(String filePath) {
    List<Reader> readers = new ArrayList<>();
    try (BufferedReader reader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = reader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] parts = line.split(FIELD_SEPARATOR);
        String name = parts[READER_NAME_INDEX].strip();
        int limit = Integer.parseInt(parts[READER_LIMIT_INDEX].strip());
        readers.add(new Reader(name, limit));
      }
    } catch (IOException e) {
      logger.error("Failed to read readers data from file: {}", filePath, e);
      throw new RuntimeException("Cannot load readers data", e);
    }
    logger.info("Loaded {} readers", readers.size());
    return readers;
  }
}
