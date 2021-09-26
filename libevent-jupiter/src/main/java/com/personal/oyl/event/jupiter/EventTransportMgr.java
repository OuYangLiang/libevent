package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventPusher;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author OuYang Liang
 */
public class EventTransportMgr {

    private static final Logger log = LoggerFactory.getLogger(EventTransportMgr.class);

    private final Map<Integer, Thread> currentRunning = new HashMap<>();
    private final List<Integer> chargingTables = new LinkedList<>();
    private final EventMapper mapper;
    private final EventPusher pusher;

    public EventTransportMgr(EventMapper mapper, EventPusher pusher) {
        this.mapper = mapper;
        this.pusher = pusher;
    }

    synchronized void assign(String assignment) {
        log.info("Receive assignment {} from Master ...", assignment);
        this.stopAll();
        String[] parts = assignment.split(JupiterConfiguration.GROUP_SEPARATOR);

        for (String part : parts) {
            int n = Integer.parseInt(part.trim());
            log.info("Starting EventTransport thread for table {} ...", n);
            Thread submitThread = new Thread(new EventTransport(n, this.mapper, this.pusher));
            submitThread.start();
            currentRunning.put(n, submitThread);
            chargingTables.add(n);
        }

    }

    synchronized void restartAll() {
        if (currentRunning.isEmpty()) {
            log.info("Resuming all EventTransport threads ...");
            for (int n : chargingTables) {
                log.info("Resuming EventTransport thread for table {} ...", n);
                Thread submitThread = new Thread(new EventTransport(n, this.mapper, this.pusher));
                submitThread.start();
                currentRunning.put(n, submitThread);
            }
            log.info("all EventTransport threads resumed successfully ...");
        }
    }

    synchronized void stopAll() {
        log.info("Stopping all EventTransport threads ...");
        for (Map.Entry<Integer, Thread> entry : currentRunning.entrySet()) {
            log.info("Stopping EventTransport thread for table {} ...", entry.getKey());
            entry.getValue().interrupt();
        }
        log.info("all EventTransport threads stopped ...");
        currentRunning.clear();
    }

    EventMapper getMapper() {
        return mapper;
    }

    EventPusher getPusher() {
        return pusher;
    }

}
