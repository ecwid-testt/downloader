package ru.ecwid.tests.downloader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

public class TaskPool {
    private final Map<String, Task> taskByLinkMap;
    private final Map<String, Task> taskByFileNameMap;
    private final Queue<Task> taskQueue;
    private volatile int totalTaskCount;
    private volatile int doneTaskCount;
    private volatile boolean allTaskCreated;

    @Inject
    public TaskPool(@Named(ConsoleDownloaderModule.TASKS_FILE) String tasksFileName) throws IOException {
        this.taskByLinkMap = new HashMap<>();
        this.taskByFileNameMap = new HashMap<>();
        this.taskQueue = new ConcurrentLinkedQueue<>();
        this.allTaskCreated = false;
        loadFromFile(tasksFileName);
    }

    private void loadFromFile(String tasksFileName) throws IOException {
        try(BufferedReader tasksFileReader = new BufferedReader(new InputStreamReader(new FileInputStream(tasksFileName)))) {
            String taskLine;
            while ((taskLine = tasksFileReader.readLine()) != null) {
                String[] taskLineParts = taskLine.split(" ");
                if (taskLineParts.length != 2) {
                    throw new IOException("Unknown task file format, line: " + taskLine);
                }
                addTask(taskLineParts[0], taskLineParts[1]);
            }
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
            totalTaskCount++;
            taskByLinkMap.put(link, task);
            taskQueue.add(task);
        }
        taskByFileNameMap.put(fileName, task);
    }

    public synchronized Task getNextTask() {
        return taskQueue.poll();
    }

    public synchronized void onTaskDone(Task task) {
        doneTaskCount++;
    }

    public boolean isAllTasksDone() {
        return doneTaskCount >= totalTaskCount;
    }

    public boolean isAllTaskCreated() {
        return allTaskCreated;
    }

    public void allTaskCreated() {
        allTaskCreated = true;
    }

}
