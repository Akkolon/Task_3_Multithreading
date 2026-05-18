package com.bazylev.library.service;

import com.bazylev.library.entity.Book;
import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Reader;
import com.bazylev.library.exception.BookNotAvailableException;
import com.bazylev.library.state.ReaderState;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.Callable;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LibraryService {

  private static final Logger logger = LogManager.getLogger(LibraryService.class);

  private static final int READING_TIME_SECONDS = 2;
  private static final int WAIT_BETWEEN_ATTEMPTS_MS = 300;

  private final Library library;
  private final ReentrantLock serviceLock;
  private final Condition bookAvailable;

  public LibraryService(Library library) {
    this.library = library;
    this.serviceLock = new ReentrantLock();
    this.bookAvailable = serviceLock.newCondition();
  }

  public Callable<Void> createReaderTask(Reader reader) {
    return () -> {
      logger.info("{} has entered the library", reader.getName());
      reader.setState(ReaderState.SELECTING_BOOKS);

      borrowBooks(reader);

      reader.setState(ReaderState.READING);
      logger.info("{} is reading. Borrowed books: {}", reader.getName(), reader.getBorrowedBooks());
      TimeUnit.SECONDS.sleep(READING_TIME_SECONDS);

      reader.setState(ReaderState.RETURNING_BOOKS);
      returnBooks(reader);

      reader.setState(ReaderState.SERVED);
      logger.info("{} has been fully served and left the library", reader.getName());

      return null;
    };
  }

  private void borrowBooks(Reader reader) throws InterruptedException, BookNotAvailableException {
    int booksToTake = reader.getBookLimit();
    int taken = 0;

    while (taken < booksToTake) {
      serviceLock.lock();
      try {
        while (library.getAvailableBooksCount() == 0) {
          logger.info("{} is waiting for a book to become available", reader.getName());
          bookAvailable.await(WAIT_BETWEEN_ATTEMPTS_MS, TimeUnit.MILLISECONDS);
        }
        Book book = library.borrowBook();
        reader.addBorrowedBook(book);
        taken++;
        logger.info("{} borrowed book {}/{}: {}", reader.getName(), taken, booksToTake, book);
      } finally {
        serviceLock.unlock();
      }
    }
  }

  private void returnBooks(Reader reader) {
    serviceLock.lock();
    try {
      for (Book book : reader.getBorrowedBooks()) {
        library.returnBook(book);
        logger.info("{} returned book: {}", reader.getName(), book);
      }
      reader.clearBorrowedBooks();
      bookAvailable.signalAll();
    } finally {
      serviceLock.unlock();
    }
  }
}
