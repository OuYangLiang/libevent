package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.Event;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * @author OuYang Liang
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
            List<Event> events = null;
            try {
                events = this.manager.getMapper().queryTopN(tbNum, 100);
            } catch (Exception e) {
                log.error(e.getMessage(), e);
            }

            if (null == events || events.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(1);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
            } else {
                List<String> successIds = null;
                try {
                    successIds = this.manager.getPusher().push(tbNum, events);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (null != successIds && !successIds.isEmpty()) {
                    try {
                        this.manager.getMapper().batchClean(tbNum, successIds);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
    }
}
