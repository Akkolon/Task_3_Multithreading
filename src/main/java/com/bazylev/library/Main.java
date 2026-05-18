package com.bazylev.library;

import com.bazylev.library.entity.Library;
import com.bazylev.library.entity.Reader;
import com.bazylev.library.parser.DataParser;
import com.bazylev.library.service.LibraryService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

public class Main {

  private static final Logger logger = LogManager.getLogger(Main.class);

  public static void main(String[] args) {
    DataParser parser = new DataParser();
    Library library = parser.parseLibrary("data/library.txt");
    List<Reader> readers = parser.parseReaders("data/readers.txt");

    LibraryService service = new LibraryService(library);

    ExecutorService executor = Executors.newFixedThreadPool(readers.size());
    List<Future<Void>> futures = readers.stream()
        .map(reader -> executor.submit(service.createReaderTask(reader)))
        .toList();

    futures.forEach(future -> {
      try {
        future.get();
      } catch (Exception e) {
        logger.error("Error while waiting for reader task to complete", e);
      }
    });

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

    logger.info("All readers have been served. Library session complete.");
  }
}
