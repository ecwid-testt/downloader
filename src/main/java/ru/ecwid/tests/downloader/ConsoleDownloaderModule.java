package ru.ecwid.tests.downloader;

import com.google.inject.AbstractModule;
import com.google.inject.Provides;
import com.google.inject.Singleton;
import com.google.inject.name.Named;
import com.google.inject.name.Names;

import org.apache.commons.cli.CommandLine;
import org.apache.commons.cli.CommandLineParser;
import org.apache.commons.cli.DefaultParser;
import org.apache.commons.cli.HelpFormatter;
import org.apache.commons.cli.Option;
import org.apache.commons.cli.Options;
import org.apache.commons.cli.ParseException;

import java.util.Properties;

public class ConsoleDownloaderModule extends AbstractModule {

    public static final int K = 1024;

    public static final String THREADS_COUNT = "threadsCount";
    public static final String TRAFFIC_LIMIT = "trafficLimit";
    public static final String TASKS_FILE = "tasksFile";
    public static final String OUTPUT_DIR = "outputDir";
    public static final String DOWNLOAD_PART_SIZE = "downloadPartSize";
    public static final String MINIMAL_DOWNLOAD_PART_SIZE = "minimalDownloadPartSize";

    private final Properties properties = new Properties();

    public ConsoleDownloaderModule(String[] args) throws ParseException {
        parceCommandLine(args);
    }

    private void parceCommandLine(String[] args) throws ParseException {
        // Как ругаться в консольку при неверных входных параметрах- вопрос не очень понятный.
        // Тут ругаемся не оень прилично, но вроде как понятно для пользователя
        Options options = new Options();
        options.addOption(new Option("n", "threads", true, "number of download threads"));
        options.addOption(new Option("l", "limin", true, "network traffic limit, bytes/kilobytes/megabytes (100/123k/23m)"));
        options.addOption(new Option("f", "file", true, "links file name"));
        options.addOption(new Option("o", "output", true, "dir for output files"));
        CommandLineParser parser = new DefaultParser();
        try {
            CommandLine commandLine = parser.parse(options, args);
            if (!commandLine.hasOption('n') || !commandLine.hasOption('l') || !commandLine.hasOption('f') || !commandLine.hasOption('o')) {
                printHelp(options);
                throw new ParseException("Not another parameters");
            }
            String threadsCountString = commandLine.getOptionValue('n');
            try {
                properties.put(THREADS_COUNT, Integer.parseInt(threadsCountString));
            } catch (NumberFormatException e) {
                System.out.println("Error in threads: " + threadsCountString + " not a number");
                printHelp(options);
                throw e;
            }
            final String limitString = commandLine.getOptionValue('l');
            try {
                properties.put(TRAFFIC_LIMIT, parseSize(limitString));
            } catch (NumberFormatException e) {
                System.out.println("Error in limit: " + limitString + " not a size");
                printHelp(options);
                throw e;
            }
            properties.put(TASKS_FILE, commandLine.getOptionValue('f'));
            properties.put(OUTPUT_DIR, commandLine.getOptionValue('o'));
        } catch (ParseException e) {
            printHelp(options);
            throw e;
        }
    }

    private int parseSize(String limitString) {
        limitString = limitString.toLowerCase();
        int multiply = 1;
        if (limitString.endsWith("g")) { //  Всякие 1M25K жёстко пресекаются
            multiply = K * K * K;
        } else if (limitString.endsWith("m")) {
            multiply = K * K;
        } else if (limitString.endsWith("k")) {
            multiply = K;
        }
        if (multiply > 1) {
            limitString = limitString.substring(0, limitString.length()-1);
        }
        return multiply * Integer.parseInt(limitString);
    }

    private void printHelp(Options options) {
        new HelpFormatter().printHelp("ConsoleDownloader", options);
    }

    @Provides
    @Named(DOWNLOAD_PART_SIZE)
    @Singleton
    public int downloadPartSize(@Named(TRAFFIC_LIMIT) int trafficLimit, @Named(THREADS_COUNT) int threadsCount) {
        int partSize = trafficLimit / threadsCount;
        if (partSize < 1) { // Мало ли идиоты...
            return 1;
        }
        if (partSize < K) { // Не мельчим
            return partSize;
        }
        // Возьмём с потолка оптимальный размер куска, чтобы не меньше 1К, но стараться не больше, чем 1/10 от лимита на один потом - чтобы ровнее ограничивать
        partSize /= 10;
        if (partSize < K) {
            return K;
        }
        return partSize;
    }

    @Provides
    @Named(MINIMAL_DOWNLOAD_PART_SIZE)
    @Singleton
    public int minimalDownloadPartSize(@Named(DOWNLOAD_PART_SIZE) int downloadPartSize) {
        return Math.min(1, downloadPartSize / 10);
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), properties);
    }

}
