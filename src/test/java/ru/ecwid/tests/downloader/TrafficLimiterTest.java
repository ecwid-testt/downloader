package ru.ecwid.tests.downloader;

import org.junit.Assert;
import org.junit.Test;

public class TrafficLimiterTest {

    @Test
    public void testModule() throws InterruptedException {
        TrafficLimiter tl = new TrafficLimiter(1000);
        long start = System.currentTimeMillis();
        Assert.assertEquals(800, tl.waitAndGetLimit(800));
        Assert.assertEquals(200, tl.waitAndGetLimit(800));
        long beforePause = System.currentTimeMillis();
        Assert.assertEquals(700, tl.waitAndGetLimit(700));
        long afterPause = System.currentTimeMillis();
        Assert.assertEquals(100, tl.waitAndGetLimit(800));
        long afterNoPause = System.currentTimeMillis();
        Assert.assertTrue(beforePause - start < 1000);
        Assert.assertTrue(afterPause - start >= 1000);
        Assert.assertTrue(afterNoPause - afterPause < 1000);
    }
}
