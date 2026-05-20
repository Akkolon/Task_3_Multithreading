package com.bazylev.library.entity;

import com.bazylev.library.state.VisitorState;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class VisitorTest {

  @Test
  void constructor_setsInitialStateToWaiting() {
    Visitor visitor = new Visitor("Alice", 2);

    assertEquals(VisitorState.WAITING, visitor.getState());
  }

  @Test
  void setState_changesState() {
    Visitor visitor = new Visitor("Alice", 2);

    visitor.setState(VisitorState.READING);

    assertEquals(VisitorState.READING, visitor.getState());
  }

  @Test
  void addBorrowedBook_bookAppearsInList() {
    Visitor visitor = new Visitor("Alice", 2);
    Book book = new Book("1984", "Orwell");

    visitor.addBorrowedBook(book);

    assertTrue(visitor.getBorrowedBooks().contains(book));
  }

  @Test
  void getBorrowedBooks_returnsUnmodifiableList() {
    Visitor visitor = new Visitor("Alice", 2);
    visitor.addBorrowedBook(new Book("1984", "Orwell"));

    List<Book> books = visitor.getBorrowedBooks();

    assertThrows(UnsupportedOperationException.class, books::clear);
  }

  @Test
  void clearBorrowedBooks_emptiesList() {
    Visitor visitor = new Visitor("Alice", 2);
    visitor.addBorrowedBook(new Book("1984", "Orwell"));

    visitor.clearBorrowedBooks();

    assertTrue(visitor.getBorrowedBooks().isEmpty());
  }

  @Test
  void getBookLimit_returnsConstructorValue() {
    Visitor visitor = new Visitor("Bob", 3);

    assertEquals(3, visitor.getBookLimit());
  }
}
