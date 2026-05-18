package com.bazylev.library.entity;

import com.bazylev.library.exception.BookNotAvailableException;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.concurrent.locks.ReentrantLock;

public class Library {

  private static final Logger logger = LogManager.getLogger(Library.class);

  private static final AtomicReference<Library> INSTANCE = new AtomicReference<>();

  private final List<Book> availableBooks;
  private final ReentrantLock libraryLock;

  private Library() {
    this.availableBooks = new ArrayList<>();
    this.libraryLock = new ReentrantLock();
  }

  public static Library getInstance() {
    Library existing = INSTANCE.get();
    if (existing != null) {
      return existing;
    }
    Library newInstance = new Library();
    if (INSTANCE.compareAndSet(null, newInstance)) {
      return newInstance;
    }
    return INSTANCE.get();
  }

  public void addBook(Book book) {
    libraryLock.lock();
    try {
      availableBooks.add(book);
    } finally {
      libraryLock.unlock();
    }
  }

  public Book borrowBook() throws BookNotAvailableException {
    libraryLock.lock();
    try {
      if (!availableBooks.isEmpty()) {
        Book book = availableBooks.remove(0);
        logger.debug("Book {} has been taken from the library shelf", book);
        return book;
      } else {
        throw new BookNotAvailableException("No books available in the library");
      }
    } finally {
      libraryLock.unlock();
    }
  }

  public void returnBook(Book book) {
    libraryLock.lock();
    try {
      availableBooks.add(book);
      logger.debug("Book {} has been returned to the library", book);
    } finally {
      libraryLock.unlock();
    }
  }

  public int getAvailableBooksCount() {
    libraryLock.lock();
    try {
      return availableBooks.size();
    } finally {
      libraryLock.unlock();
    }
  }

  public List<Book> getAvailableBooks() {
    libraryLock.lock();
    try {
      return Collections.unmodifiableList(availableBooks);
    } finally {
      libraryLock.unlock();
    }
  }
}
