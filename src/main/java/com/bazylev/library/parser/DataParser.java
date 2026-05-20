package com.bazylev.library.parser;

import com.bazylev.library.entity.Book;
import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Visitor;
import com.bazylev.library.exception.DataParseException;
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
  private static final int VISITOR_NAME_INDEX = 0;
  private static final int VISITOR_LIMIT_INDEX = 1;

  public Library parseLibrary(String filePath) throws DataParseException {
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
      throw new DataParseException("Failed to read library data from file: " + filePath, e);
    }
    logger.info("Library loaded with {} books", library.getAvailableBooksCount());
    return library;
  }

  public List<Visitor> parseVisitors(String filePath) throws DataParseException {
    List<Visitor> visitors = new ArrayList<>();
    try (BufferedReader fileReader = new BufferedReader(new FileReader(filePath))) {
      String line;
      while ((line = fileReader.readLine()) != null) {
        if (line.isBlank()) {
          continue;
        }
        String[] parts = line.split(FIELD_SEPARATOR);
        String name = parts[VISITOR_NAME_INDEX].strip();
        int limit = Integer.parseInt(parts[VISITOR_LIMIT_INDEX].strip());
        visitors.add(new Visitor(name, limit));
      }
    } catch (IOException e) {
      throw new DataParseException("Failed to read visitors data from file: " + filePath, e);
    }
    logger.info("Loaded {} visitors", visitors.size());
    return visitors;
  }
}
