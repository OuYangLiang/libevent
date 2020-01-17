package com.personal.oyl.event.jupiter;

import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventPusher;

import java.util.LinkedList;
import java.util.List;

/**
 * @author OuYang Liang
 */
public class EventTransportMgr {

    private List<Thread> currentRunning = new LinkedList<>();
    private EventMapper mapper;
    private EventPusher pusher;

    public EventTransportMgr(EventMapper mapper, EventPusher pusher) {
        this.mapper = mapper;
        this.pusher = pusher;
    }

    void start(int n) {
        Thread submitThread = new Thread(new EventTransport(n, this));
        submitThread.start();
        currentRunning.add(submitThread);
    }

    void stopAll() {
        for (Thread t : currentRunning) {
            t.interrupt();
        }

        currentRunning.clear();
    }

    EventMapper getMapper() {
        return mapper;
    }

    EventPusher getPusher() {
        return pusher;
    }

}
