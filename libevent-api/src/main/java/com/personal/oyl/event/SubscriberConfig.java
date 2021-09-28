package com.personal.oyl.event;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author OuYang Liang
 */
public enum SubscriberConfig {
    instance;
    
    private final Map<String, List<EventSubscriber>> cfg = new HashMap<>();

    public List<EventSubscriber> getSubscribers(String eventType) {
        if (this.cfg.containsKey(eventType)) {
            return this.cfg.get(eventType);
        }
        
        return Collections.emptyList();
    }
    
    public void addSubscriber(String eventType, EventSubscriber sub) {
        if (this.cfg.containsKey(eventType)) {
            this.cfg.get(eventType).add(sub);
        } else {
            List<EventSubscriber> list = new LinkedList<>();
            list.add(sub);
            this.cfg.put(eventType, list);
        }
    }
}
