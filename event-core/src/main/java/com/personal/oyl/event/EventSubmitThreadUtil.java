package com.personal.oyl.event;

import java.util.LinkedList;
import java.util.List;

import org.springframework.stereotype.Component;

@Component
public class EventSubmitThreadUtil {
    private List<Thread> currentRunning = new LinkedList<>();
    
    public void startForN(int n) {
        Thread submitThread = new Thread(new EventSubmitter(n));
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
