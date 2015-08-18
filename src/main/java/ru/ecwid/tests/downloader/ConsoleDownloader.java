package ru.ecwid.tests.downloader;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import org.apache.commons.cli.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

public class ConsoleDownloader implements Runnable {
    private static final Logger LOG = LoggerFactory.getLogger(ConsoleDownloader.class);

    @Inject
    @Named(ConsoleDownloaderModule.TASKS_FILE)
    private String tasksFileName;

    @Inject
    @Named(ConsoleDownloaderModule.THREADS_COUNT)
    private int threadsCount;

    @Inject
    private TaskDownloaderRunnable taskDownloaderRunnable;

    @Inject
    private TaskPool taskPool;

    @Inject
    public ConsoleDownloader() {
    }

    @Override
    public void run() {
        ExecutorService downloaderExecutorService = Executors.newFixedThreadPool(threadsCount);
        try {
            taskPool.loadFromFile(tasksFileName);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        for (final Task task : taskPool.getTasks()) {
            downloaderExecutorService.submit(() -> taskDownloaderRunnable.run(task));
        }
        try {
            downloaderExecutorService.awaitTermination(24, TimeUnit.HOURS);
        } catch (InterruptedException e) {
            downloaderExecutorService.shutdown();
        }
        long totalSize = 0;
        long linkCount = 0;
        long fileCount = 0;
        long errorCount = 0;
        for (final Task task : taskPool.getTasks()) {
            if (task.isOk()) {
                linkCount++;
                fileCount += task.getFileNameSet().size();
                totalSize += task.getFileSize();
            } else {
                errorCount++;
                LOG.warn("Error {} on load {}", task.getErrorMessage(), task.getLink());
            }
        }
        LOG.info("Loaded {} bytes from {} link to {} files with [] errors", totalSize, linkCount, fileCount, errorCount);
    }

    public static void main(String[] args) throws ParseException {
        Injector injector = Guice.createInjector(new ConsoleDownloaderModule(args));
        injector.getInstance(ConsoleDownloader.class).run();
    }

}
