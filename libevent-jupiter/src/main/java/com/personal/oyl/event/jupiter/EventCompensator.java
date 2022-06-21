package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventReceiver;
import com.personal.oyl.event.FailedEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class EventCompensator implements Runnable {
    private static final Logger log = LoggerFactory.getLogger(EventCompensator.class);

    private final EventMapper mapper;
    private final EventReceiver receiver;

    public EventCompensator(EventMapper mapper, EventReceiver receiver) {
        this.mapper = mapper;
        this.receiver = receiver;
    }

    @Override
    public void run() {
        log.info("Event Compensator job started...");

        while (!Thread.currentThread().isInterrupted()) {
            List<FailedEvent> eventList = mapper.queryFailedEvent(100);
            if (eventList == null || eventList.isEmpty()) {
                try {
                    TimeUnit.SECONDS.sleep(20);
                } catch (InterruptedException e) {
                    Thread.currentThread().interrupt();
                }
                continue;
            }

            Map<String, List<FailedEvent>> group = eventList.stream().collect(Collectors.groupingBy(FailedEvent::key));
            for (Map.Entry<String, List<FailedEvent>> entry : group.entrySet()) {
                for (FailedEvent failedEvent : entry.getValue()) {
                    boolean succ = receiver.retryEvent(failedEvent);
                    if (!succ) {
                        break;
                    }
                }
            }
        }
    }
}