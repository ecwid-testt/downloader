package ru.ecwid.tests.downloader;

import com.google.inject.Inject;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class TaskPool {
    private final Map<String, Task> taskByLinkMap;
    private final Map<String, Task> taskByFileNameMap;

    @Inject
    public TaskPool() {
        this.taskByLinkMap = new HashMap<>();
        this.taskByFileNameMap = new HashMap<>();
    }

    public void loadFromFile(String tasksFileName) throws IOException {
        try(BufferedReader tasksFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(tasksFileName)))) {
            loadFromReader(tasksFileReader);
        }
    }

    public void loadFromReader(BufferedReader tasksFileReader) throws IOException {
        String taskLine;
        while ((taskLine = tasksFileReader.readLine()) != null) {
            String[] taskLineParts = taskLine.split(" ");
            if (taskLineParts.length != 2) {
                throw new IOException("Unknown task file format, line: " + taskLine);
            }
            addTask(taskLineParts[0], taskLineParts[1]);
        }
    }

    private void addTask(String link, String fileName) {
        Task task = taskByFileNameMap.get(fileName);
        if (task != null) {
            if (task.getLink().equals(link)) {
                return; // Чистый дубль- пусть будет
            } else { // Хотят странного - в одни файл загрузить две разные ссылки. Мы такого не умеем
                throw new RuntimeException("Can't save from " + link + " and " + task.getLink() + " to " + fileName);
            }
        }
        task = taskByLinkMap.get(link);
        if (task != null) {
            task.addFileName(fileName);
        } else {
            task = new Task(link, fileName);
            taskByLinkMap.put(link, task);
        }
        taskByFileNameMap.put(fileName, task);
    }

    public Collection<Task> getTasks() {
        return taskByLinkMap.values();
    }

}
