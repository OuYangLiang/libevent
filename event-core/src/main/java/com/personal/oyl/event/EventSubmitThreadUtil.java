package com.personal.oyl.event;

import java.util.LinkedList;
import java.util.List;

public class EventSubmitThreadUtil {
    private List<Thread> currentRunning = new LinkedList<>();
    private EventMapper mapper;
    
    public EventSubmitThreadUtil(EventMapper mapper) {
        this.mapper = mapper;
    }
    
    public void startForN(int n) {
        Thread submitThread = new Thread(new EventSubmitter(n, mapper));
        submitThread.start();
        currentRunning.add(submitThread);
    }
    
    public void stopAll() {
        for (Thread t : currentRunning) {
            t.interrupt();
        }
        
        currentRunning.clear();
    }
}
