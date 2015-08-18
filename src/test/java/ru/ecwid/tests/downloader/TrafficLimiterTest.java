package ru.ecwid.tests.downloader;

import org.junit.Assert;
import org.junit.Test;

public class TrafficLimiterTest {

    @Test
    public void testModule() throws InterruptedException {
        TrafficLimiter tl = new TrafficLimiter(1000, 200);
        long start = System.currentTimeMillis();
        Assert.assertEquals(600, tl.waitAndGetLimit(600));
        // Чтобы не ждать- мы разрешим скачать сколько можно- т.е. 1000-600 байт
        Assert.assertEquals(400, tl.waitAndGetLimit(600));
        // И паузы нет
        long beforePause = System.currentTimeMillis();
        Assert.assertEquals(500, tl.waitAndGetLimit(500));
        long afterPause = System.currentTimeMillis();
        Assert.assertEquals(100, tl.waitAndGetLimit(100));
        long afterNoPause = System.currentTimeMillis();
        Assert.assertTrue(beforePause - start < 1000);
        Assert.assertTrue(afterPause - start >= 1000);
        Assert.assertTrue(afterNoPause - afterPause < 1000);
    }

    @Test
    public void testMinimalDownloadPartSize() throws InterruptedException {
        TrafficLimiter tl = new TrafficLimiter(1000, 500);
        long start = System.currentTimeMillis();
        Assert.assertEquals(600, tl.waitAndGetLimit(600));
        // Т.к. минимальный кусок- 500, то 400 нам не предложат...
        Assert.assertEquals(600, tl.waitAndGetLimit(600));
        // ...а заставят подождать
        long afterPause = System.currentTimeMillis();
        Assert.assertTrue(afterPause - start >= 1000);
    }

}
