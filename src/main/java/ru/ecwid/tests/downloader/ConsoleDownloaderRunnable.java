package ru.ecwid.tests.downloader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.BufferedOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class ConsoleDownloaderRunnable implements Runnable {

    public static final int NETWORK_TIMEOUT = 5000;
    public static final int NO_TASKS_SLEEP_TIME_MSECS = 1000;
    private final TaskPool taskPool;
    private final TrafficLimiter trafficLimiter;
    private final int downloadPartSize;
    private final String outputDir;

    @Inject
    public ConsoleDownloaderRunnable(TaskPool taskPool, TrafficLimiter trafficLimiter,
                                     @Named(ConsoleDownloaderModule.DOWNLOAD_PART_SIZE) int downloadPartSize,
                                     @Named(ConsoleDownloaderModule.OUTPUT_DIR) String outputDir) {
        this.taskPool = taskPool;
        this.trafficLimiter = trafficLimiter;
        this.downloadPartSize = downloadPartSize;
        this.outputDir = outputDir;
    }

    @Override
    public void run() {
        while (!Thread.interrupted()) {
            Task task = taskPool.getNextTask();
            try {
                if (task != null) {
                    try {
                        saveIntoFiles(task, downloadFromLink(task));
                    } catch (IOException e) {
                        //TODO Разбирать ошибки- при сетевых повторить попытку, при нехватке места, или прав- завершаться
                        task.setErrorMessage(e.getMessage());
                        taskPool.onTaskDone(task);
                    }
                } else {
                    Thread.sleep(NO_TASKS_SLEEP_TIME_MSECS);
                }
            } catch (InterruptedException e) {
                return;
            }
        }
    }

    private void saveIntoFiles(Task task, Path tempFilePath) throws IOException {
        String firstFileName = null;
        for (String fileName : task.getFileNameSet()) {
            if (firstFileName == null) {
                firstFileName = fileName;
                Files.move(tempFilePath, Paths.get(outputDir, firstFileName));
            } else {
                // Лучше всё же копировать, но для тестовой задачи будем экономить место на диске
                Files.createSymbolicLink(Paths.get(outputDir, firstFileName), Paths.get(outputDir, fileName));
            }
        }
    }

    private Path downloadFromLink(Task task) throws IOException, InterruptedException {
        URL url = new URL(task.getLink());
        HttpURLConnection connection = (HttpURLConnection) url.openConnection();
        connection.setConnectTimeout(NETWORK_TIMEOUT);
        connection.setReadTimeout(NETWORK_TIMEOUT);
        connection.connect();
        int code = connection.getResponseCode();
        if (code / 100 != 2) {
            throw new IOException("Error " + code + " on URL: " + task.getLink());
        }
        byte[] buffer = new byte[downloadPartSize];
        Path tempFilePath = Paths.get(outputDir, UUID.randomUUID().toString());
        try (OutputStream outputStream = new BufferedOutputStream(new FileOutputStream(tempFilePath.toFile()))) {
            try(InputStream inputStream = connection.getInputStream()) {
                int partSize = trafficLimiter.waitAndGetLimit(downloadPartSize);
                partSize = inputStream.read(buffer, 0, partSize);
                outputStream.write(buffer, 0, partSize);
            }
        }
        return tempFilePath;
    }
}
