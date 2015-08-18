package ru.ecwid.tests.downloader;

import com.google.inject.Inject;
import com.google.inject.name.Named;

import org.apache.commons.lang3.tuple.ImmutablePair;
import org.apache.commons.lang3.tuple.Pair;

import java.util.LinkedList;
import java.util.Queue;

/**
 * Класс, обеспечивающий выполнения ограничения на трафик.
 * Ограничения- он учитывает попытки скачать и время тоже берёт для запроса, а не для ответа.
 * Оба ограничения связаны с упрощением логики с одной стороны, а с другой- это не есть бага,
 * т.к. во-первых подгадать время прихода мы всё оно не сможем,
 * а во-вторых если ответ не пришёл- мы отвалимся по таймауту скорее всего, т.е. лимит всё равно будет съеден
 */
public class TrafficLimiter {

    private final long trafficLimit;
    private final long minimalDownloadPartSize;
    private final Queue<Pair<Long, Long>> trafficHistory;

    private long currentTraffic;
    private Pair<Long, Long> oldestTrafficElem;

    @Inject
    public TrafficLimiter(@Named(ConsoleDownloaderModule.TRAFFIC_LIMIT) long trafficLimit,
                          @Named(ConsoleDownloaderModule.MINIMAL_DOWNLOAD_PART_SIZE)  long minimalDownloadPartSize) {
        this.trafficLimit = trafficLimit;
        this.minimalDownloadPartSize = minimalDownloadPartSize;
        this.currentTraffic = 0;
        this.trafficHistory = new LinkedList<Pair<Long, Long>>();
        this.oldestTrafficElem = null;
    }

    /**
     * Подождать возможности отхватить кусок partSize от лимита и отхватить сколько можно (больше нуля)
     * Может застрять в пределах секунды (если лимит исчерпан)
     */
    public synchronized long waitAndGetLimit(long partSize) throws InterruptedException {
        // Синхронизация, т.к. если лимит исчерпан- всё одно делать нечего- встаём в очередь.
        // А что справедливости нет- ну так её вообще нет
        final long now = System.currentTimeMillis();
        if (partSize > trafficLimit - currentTraffic) { // Если же до конца далеко- не тормозим, а работаем
            if (oldestTrafficElem == null) {
                oldestTrafficElem = trafficHistory.poll();
            }
            final long oldestTrafficTimestamp = now - 1000;
            while (oldestTrafficElem != null && oldestTrafficElem.getKey() < oldestTrafficTimestamp) {
                removeOldestTrafficElem();
            }
            if (minimalDownloadPartSize > trafficLimit - currentTraffic) {
                if (oldestTrafficElem != null) {
                    Thread.sleep(oldestTrafficElem.getKey() - oldestTrafficTimestamp);
                    removeOldestTrafficElem();
                }
            } else if (partSize > trafficLimit - currentTraffic) {
                partSize = trafficLimit - currentTraffic;
            }
        }
        currentTraffic += partSize;
        trafficHistory.add(new ImmutablePair<Long, Long>(System.currentTimeMillis(), partSize));
        return partSize;
    }

    private void removeOldestTrafficElem() {
        currentTraffic -= oldestTrafficElem.getValue();
        oldestTrafficElem = trafficHistory.poll();
    }
}
