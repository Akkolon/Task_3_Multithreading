package com.bazylev.library.entity;

import com.bazylev.library.exception.BookNotAvailableException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertThrows;

class LibraryTest {

  @BeforeEach
  void resetSingleton() throws Exception {
    Field instanceField = Library.class.getDeclaredField("INSTANCE");
    instanceField.setAccessible(true);
    ((AtomicReference<?>) instanceField.get(null)).set(null);
  }

  @Test
  void getInstance_returnsSameInstance() {
    Library first = Library.getInstance();
    Library second = Library.getInstance();

    assertSame(first, second);
  }

  @Test
  void addBook_increasesAvailableCount() {
    Library library = Library.getInstance();
    library.addBook(new Book("Title", "Author"));

    assertEquals(1, library.getAvailableBooksCount());
  }

  @Test
  void borrowBook_returnsAddedBook() throws BookNotAvailableException {
    Library library = Library.getInstance();
    Book book = new Book("Clean Code", "Robert Martin");
    library.addBook(book);

    Book borrowed = library.borrowBook();

    assertSame(book, borrowed);
  }

  @Test
  void borrowBook_decreasesAvailableCount() throws BookNotAvailableException {
    Library library = Library.getInstance();
    library.addBook(new Book("Title", "Author"));

    library.borrowBook();

    assertEquals(0, library.getAvailableBooksCount());
  }

  @Test
  void borrowBook_throwsWhenEmpty() {
    Library library = Library.getInstance();

    assertThrows(BookNotAvailableException.class, library::borrowBook);
  }

  @Test
  void returnBook_increasesAvailableCount() throws BookNotAvailableException {
    Library library = Library.getInstance();
    library.addBook(new Book("Title", "Author"));
    Book book = library.borrowBook();

    library.returnBook(book);

    assertEquals(1, library.getAvailableBooksCount());
  }

  @Test
  void getAvailableBooks_returnsUnmodifiableList() {
    Library library = Library.getInstance();
    library.addBook(new Book("Title", "Author"));

    assertThrows(UnsupportedOperationException.class,
        () -> library.getAvailableBooks().clear());
  }
}
