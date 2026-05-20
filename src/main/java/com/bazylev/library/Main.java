package com.bazylev.library;

import com.bazylev.library.entity.Visitor;
import com.bazylev.library.exception.DataParseException;
import com.bazylev.library.parser.DataParser;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    DataParser parser = new DataParser();
    List<Visitor> visitors;

    try {
      parser.parseLibrary("data/library.txt");
      visitors = parser.parseVisitors("data/readers.txt");
    } catch (DataParseException e) {
      logger.fatal("Failed to load input data: {}", e.getMessage());
      return;
    }

    ExecutorService executor = Executors.newFixedThreadPool(visitors.size());
    try {
      logger.info("Library session started. Visitors: {}", visitors.size());
      executor.invokeAll(visitors);
    } catch (InterruptedException e) {
      logger.error("Main thread was interrupted", e);
      Thread.currentThread().interrupt();
    } finally {
      shutdownExecutor(executor);
    }

    logger.info("All visitors have been served. Library session complete.");
  }

  private static void shutdownExecutor(ExecutorService executor) {
    executor.shutdown();
    try {
      if (!executor.awaitTermination(60, TimeUnit.SECONDS)) {
        logger.warn("Executor did not terminate in time, forcing shutdown");
        executor.shutdownNow();
      }
    } catch (InterruptedException e) {
      logger.error("Interrupted while waiting for executor to terminate", e);
      executor.shutdownNow();
      Thread.currentThread().interrupt();
    }
  }
}
