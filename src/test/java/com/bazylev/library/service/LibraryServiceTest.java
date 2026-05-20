package com.bazylev.library.service;

import com.bazylev.library.entity.Book;
import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Visitor;
import com.bazylev.library.exception.BookNotAvailableException;
import com.bazylev.library.state.VisitorState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class LibraryServiceTest {

  @BeforeEach
  void resetLibrarySingleton() {
    Library.resetForTesting();
  }

  @Test
  void borrowBooks_visitorReceivesRequestedNumberOfBooks()
          throws InterruptedException, BookNotAvailableException {
    Library.getInstance().addBook(new Book("1984", "Orwell"));
    Library.getInstance().addBook(new Book("Dune", "Herbert"));
    Visitor visitor = new Visitor("Alice", 2);

    LibraryService.getInstance().borrowBooks(visitor);

    assertEquals(2, visitor.getBorrowedBooks().size());
  }

  @Test
  void borrowBooks_libraryCountDecreasesAfterBorrow()
          throws InterruptedException, BookNotAvailableException {
    Library.getInstance().addBook(new Book("1984", "Orwell"));
    Library.getInstance().addBook(new Book("Dune", "Herbert"));
    Visitor visitor = new Visitor("Alice", 1);

    LibraryService.getInstance().borrowBooks(visitor);

    assertEquals(1, Library.getInstance().getAvailableBooksCount());
  }

  @Test
  void returnBooks_libraryCountRestored()
          throws InterruptedException, BookNotAvailableException {
    Library.getInstance().addBook(new Book("1984", "Orwell"));
    Visitor visitor = new Visitor("Alice", 1);
    LibraryService.getInstance().borrowBooks(visitor);

    LibraryService.getInstance().returnBooks(visitor);

    assertEquals(1, Library.getInstance().getAvailableBooksCount());
  }

  @Test
  void returnBooks_visitorListEmptyAfterReturn()
          throws InterruptedException, BookNotAvailableException {
    Library.getInstance().addBook(new Book("1984", "Orwell"));
    Visitor visitor = new Visitor("Alice", 1);
    LibraryService.getInstance().borrowBooks(visitor);

    LibraryService.getInstance().returnBooks(visitor);

    assertTrue(visitor.getBorrowedBooks().isEmpty());
  }

  @Test
  void fullVisitorFlow_stateIsServedAtEnd() throws Exception {
    Library.getInstance().addBook(new Book("1984", "Orwell"));
    Visitor visitor = new Visitor("Alice", 1);

    visitor.call();

    assertEquals(VisitorState.SERVED, visitor.getState());
  }
}