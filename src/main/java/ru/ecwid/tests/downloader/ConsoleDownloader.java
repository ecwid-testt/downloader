package ru.ecwid.tests.downloader;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import com.google.inject.name.Named;

import org.apache.commons.cli.ParseException;

public class ConsoleDownloader implements Runnable {

    private final int threadsCount;

    @Inject
    public ConsoleDownloader(@Named(ConsoleDownloaderModule.THREADS_COUNT) int threadsCount) {
        this.threadsCount = threadsCount;
    }

    @Override
    public void run() {
        System.out.println("threadsCount=" + threadsCount);
    }

    public static void main(String[] args) throws ParseException {
        Injector injector = Guice.createInjector(new ConsoleDownloaderModule(args));
        injector.getInstance(ConsoleDownloader.class).run();
    }

}
