package com.bazylev.library.entity;

import com.bazylev.library.state.ReaderState;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Reader {

  private final String name;
  private final int bookLimit;
  private final List<Book> borrowedBooks;
  private final ReentrantLock stateLock;
  private ReaderState state;

  public Reader(String name, int bookLimit) {
    this.name = name;
    this.bookLimit = bookLimit;
    this.borrowedBooks = new ArrayList<>();
    this.stateLock = new ReentrantLock();
    this.state = ReaderState.WAITING;
  }

  public String getName() {
    return name;
  }

  public int getBookLimit() {
    return bookLimit;
  }

  public ReaderState getState() {
    stateLock.lock();
    try {
      return state;
    } finally {
      stateLock.unlock();
    }
  }

  public void setState(ReaderState state) {
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

  public int getBorrowedCount() {
    stateLock.lock();
    try {
      return borrowedBooks.size();
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
    return "Reader{name='" + name + "', limit=" + bookLimit + "}";
  }
}
