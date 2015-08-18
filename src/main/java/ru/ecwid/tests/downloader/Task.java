package ru.ecwid.tests.downloader;

import java.util.HashSet;
import java.util.Set;

public class Task {
    private final String link;
    private final Set<String> fileNameSet;

    private long fileSize;
    private String errorMessage;

    public Task(String link, String fileName) {
        this.link = link;
        this.fileNameSet = new HashSet<>();
        fileNameSet.add(fileName); //Не используется addFileName, чтобы не вызывать виртуальный метод из конструктора
    }

    public void addFileName(String fileName) {
        fileNameSet.add(fileName);
    }

    public String getLink() {
        return link;
    }

    public Set<String> getFileNameSet() {
        return fileNameSet;
    }

    public long getFileSize() {
        return fileSize;
    }

    public void setFileSize(long fileSize) {
        this.fileSize = fileSize;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    public void setErrorMessage(String errorMessage) {
        this.errorMessage = errorMessage;
    }

    public boolean hasError() {
        return errorMessage != null;
    }

}
