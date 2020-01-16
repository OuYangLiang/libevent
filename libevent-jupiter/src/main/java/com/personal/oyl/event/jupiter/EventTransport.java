package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;

/**
 * @author OuYang Liang
 * @since 2020-01-15
 */
public class EventTransport implements Runnable {

    private static final Logger log = LoggerFactory.getLogger(EventTransport.class);

    private int tbNum;
    private EventTransportMgr manager;


    public EventTransport(int tbNum, EventTransportMgr manager) {
        this.tbNum = tbNum;
        this.manager = manager;
    }

    @Override
    public void run() {
        while (!Thread.currentThread().isInterrupted()) {
            List<Event> events = this.manager.getMapper().queryTopN(tbNum, 100);

            if (null == events || events.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            try {
                List<String> successIds = this.manager.getPusher().push(tbNum, events);
                this.manager.getMapper().batchClean(tbNum, successIds);
            } catch (ExecutionException e) {
                log.error(e.getMessage(), e);
            } catch (InterruptedException e) {
                log.error(e.getMessage(), e);
            }

        }
    }
}
