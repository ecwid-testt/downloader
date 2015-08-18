package ru.ecwid.tests.downloader;

import org.apache.commons.lang3.builder.EqualsBuilder;
import org.apache.commons.lang3.builder.HashCodeBuilder;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public class Task {
    private final String link;
    private final Set<String> fileNameSet;

    private long fileSize;
    private String errorMessage;
    private boolean ok;

    public Task(String link, String... fileNameArray) {
        this.link = link;
        this.fileNameSet = new HashSet<>();
        Collections.addAll(fileNameSet, fileNameArray);
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

    public boolean isOk() {
        return ok;
    }

    public void ok() {
        this.ok = true;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;

        if (o == null || getClass() != o.getClass()) return false;

        Task task = (Task) o;

        return new EqualsBuilder()
                .append(link, task.link)
                .append(fileNameSet, task.fileNameSet)
                .isEquals();
    }

    @Override
    public int hashCode() {
        return new HashCodeBuilder(17, 37)
                .append(link)
                .append(fileNameSet)
                .toHashCode();
    }
}
