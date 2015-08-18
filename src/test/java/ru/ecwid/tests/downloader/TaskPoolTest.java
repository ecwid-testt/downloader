package ru.ecwid.tests.downloader;

import org.junit.Assert;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

import java.io.BufferedReader;
import java.io.IOException;

public class TaskPoolTest {

    @Test
    public void testOk() throws InterruptedException, IOException {
        TaskPool taskPool = new TaskPool();
        taskPool.loadFromReader(makeBufferReaderMock(new String[] {
                "http://ya.ru/ ya_ru.txt",
                "http://ya.ru/ ya_ru_1.txt",
                "https://ya.ru/ ya_ru_2.txt"}));

        Assert.assertEquals(2, taskPool.getTasks().size());
        Assert.assertTrue(taskPool.getTasks().contains(new Task("http://ya.ru/", "ya_ru.txt", "ya_ru_1.txt")));
        Assert.assertTrue(taskPool.getTasks().contains(new Task("https://ya.ru/", "ya_ru_2.txt")));
        Assert.assertFalse(taskPool.getTasks().contains(new Task("http://ya.ru/", "ya_ru_2.txt")));
    }

    @Test
    public void testException() throws InterruptedException, IOException {
        TaskPool taskPool = new TaskPool();
        try {
            taskPool.loadFromReader(makeBufferReaderMock(new String[] {
                    "http://ya.ru/ ya_ru.txt",
                    "http://ya.ru/ ya_ru_1.txt",
                    "https://ya.ru/ ya_ru.txt"}));
            Assert.fail("needs runtime exception");
        } catch (RuntimeException e) {
            //OK
        }
    }

    private BufferedReader makeBufferReaderMock(final String[] fileLines) throws IOException {
        BufferedReader bufferedReaderMock = Mockito.mock(BufferedReader.class);
        Mockito.when(bufferedReaderMock.readLine()).
                then(new Answer<String>() {
                    private int idx = 0;
                    @Override
                    public String answer(InvocationOnMock invocation) {
                        if (idx < fileLines.length) {
                            return fileLines[idx++];
                        } else {
                            return null;
                        }
                    }
                });
        return bufferedReaderMock;
    }

}
