package com.bazylev.library.service;

import com.bazylev.library.entity.Book;
import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Visitor;
import com.bazylev.library.exception.BookNotAvailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

public class LibraryService {

  private static final Logger logger = LogManager.getLogger(LibraryService.class);

  private static final int WAIT_BETWEEN_ATTEMPTS_MS = 300;

  private static class Holder {
    private static final LibraryService INSTANCE = new LibraryService();
  }

  public static LibraryService getInstance() {
    return Holder.INSTANCE;
  }

  private final ReentrantLock serviceLock;
  private final Condition bookAvailable;

  private LibraryService() {
    this.serviceLock = new ReentrantLock();
    this.bookAvailable = serviceLock.newCondition();
  }

  public void borrowBooks(Visitor visitor) throws InterruptedException, BookNotAvailableException {
    int booksToTake = visitor.getBookLimit();
    int taken = 0;

    while (taken < booksToTake) {
      serviceLock.lock();
      try {
        while (Library.getInstance().getAvailableBooksCount() == 0) {
          logger.info("{} is waiting for a book to become available", visitor.getName());
          bookAvailable.await(WAIT_BETWEEN_ATTEMPTS_MS, TimeUnit.MILLISECONDS);
        }
        Book book = Library.getInstance().borrowBook();
        visitor.addBorrowedBook(book);
        taken++;
        logger.info("{} borrowed book {}/{}: {}", visitor.getName(), taken, booksToTake, book);
      } finally {
        serviceLock.unlock();
      }
    }
  }

  public void returnBooks(Visitor visitor) {
    serviceLock.lock();
    try {
      for (Book book : visitor.getBorrowedBooks()) {
        Library.getInstance().returnBook(book);
        logger.info("{} returned book: {}", visitor.getName(), book);
      }
      visitor.clearBorrowedBooks();
      bookAvailable.signalAll();
    } finally {
      serviceLock.unlock();
    }
  }
}
