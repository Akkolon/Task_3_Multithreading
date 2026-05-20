package com.bazylev.library.entity;

import com.bazylev.library.exception.BookNotAvailableException;
import com.bazylev.library.service.LibraryService;
import com.bazylev.library.state.VisitorState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;

public class Visitor implements Callable<Void> {

  private static final Logger logger = LogManager.getLogger(Visitor.class);

  private static final int READING_TIME_SECONDS = 2;

  private final String name;
  private final int bookLimit;
  private final List<Book> borrowedBooks;
  private final ReentrantLock stateLock;
  private VisitorState state;

  public Visitor(String name, int bookLimit) {
    this.name = name;
    this.bookLimit = bookLimit;
    this.borrowedBooks = new ArrayList<>();
    this.stateLock = new ReentrantLock();
    this.state = VisitorState.WAITING;
  }

  @Override
  public Void call() throws InterruptedException, BookNotAvailableException {
    LibraryService service = LibraryService.getInstance();

    logger.info("{} has entered the library", name);
    setState(VisitorState.SELECTING_BOOKS);
    service.borrowBooks(this);

    setState(VisitorState.READING);
    logger.info("{} is reading. Borrowed books: {}", name, getBorrowedBooks());
    TimeUnit.SECONDS.sleep(READING_TIME_SECONDS);

    setState(VisitorState.RETURNING_BOOKS);
    service.returnBooks(this);

    setState(VisitorState.SERVED);
    logger.info("{} has been fully served and left the library", name);

    return null;
  }

  public String getName() {
    return name;
  }

  public int getBookLimit() {
    return bookLimit;
  }

  public VisitorState getState() {
    stateLock.lock();
    try {
      return state;
    } finally {
      stateLock.unlock();
    }
  }

  public void setState(VisitorState state) {
    stateLock.lock();
    try {
      this.state = state;
    } finally {
      stateLock.unlock();
    }
  }

  public void addBorrowedBook(Book book) {
    stateLock.lock();
    try {
      borrowedBooks.add(book);
    } finally {
      stateLock.unlock();
    }
  }

  public List<Book> getBorrowedBooks() {
    stateLock.lock();
    try {
      return Collections.unmodifiableList(borrowedBooks);
    } finally {
      stateLock.unlock();
    }
  }

  public void clearBorrowedBooks() {
    stateLock.lock();
    try {
      borrowedBooks.clear();
    } finally {
      stateLock.unlock();
    }
  }

  @Override
  public String toString() {
    return "Visitor{name='" + name + "', limit=" + bookLimit + "}";
  }
}
