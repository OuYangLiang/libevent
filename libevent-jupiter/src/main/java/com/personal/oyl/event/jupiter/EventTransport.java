package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventPusher;
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
    private EventMapper mapper;
    private EventPusher pusher;


    public EventTransport(int tbNum, EventMapper mapper, EventPusher pusher) {
        this.tbNum = tbNum;
        this.mapper = mapper;
        this.pusher = pusher;
    }

    @Override
    public void run() {
        log.info("EventTransport thread for table {} started ...", tbNum);
        while (!Thread.currentThread().isInterrupted()) {
            List<Event> events = null;
            try {
                events = this.mapper.queryTopN(tbNum, 100);
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
                    successIds = this.pusher.push(tbNum, events);
                } catch (Exception e) {
                    log.error(e.getMessage(), e);
                }

                if (null != successIds && !successIds.isEmpty()) {
                    try {
                        this.mapper.batchClean(tbNum, successIds);
                    } catch (Exception e) {
                        log.error(e.getMessage(), e);
                    }
                }
            }
        }
        log.warn("EventTransport thread for table {} stopped ...", tbNum);
    }
}
