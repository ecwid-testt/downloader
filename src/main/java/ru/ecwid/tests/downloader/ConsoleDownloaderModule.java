package ru.ecwid.tests.downloader;

import com.google.inject.AbstractModule;
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

    public static final String THREADS_COUNT = "threadsCount";
    public static final String TRAFFIC_LIMIT = "trafficLimit";
    public static final String TASKS_FILE = "tasksFile";
    public static final String OUTPUT_DIR = "outputDir";

    private final Properties properties = new Properties();

    public ConsoleDownloaderModule(String[] args) throws ParseException {
        parceCommandLine(args);
    }

    private void parceCommandLine(String[] args) throws ParseException {
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
            properties.put(THREADS_COUNT, commandLine.getOptionValue('n'));
            properties.put(TRAFFIC_LIMIT, commandLine.getOptionValue('l'));
            properties.put(TASKS_FILE, commandLine.getOptionValue('f'));
            properties.put(OUTPUT_DIR, commandLine.getOptionValue('o'));
        } catch (ParseException e) {
            printHelp(options);
            throw e;
        }
    }

    private void printHelp(Options options) {
        new HelpFormatter().printHelp("ConsoleDownloader", options);
    }

    @Override
    protected void configure() {
        Names.bindProperties(binder(), properties);
    }

}
