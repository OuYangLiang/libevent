package com.personal.oyl.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.stereotype.Component;

@Component
public class SubscriberConfig {
    private Map<String, List<BaseSubscriber>> cfg = new ConcurrentHashMap<>();

    public Map<String, List<BaseSubscriber>> getCfg() {
        return cfg;
    }

    public void setCfg(Map<String, List<BaseSubscriber>> cfg) {
        this.cfg = cfg;
    }
    
    public List<BaseSubscriber> getSubscribers(String eventType) {
        if (this.getCfg().containsKey(eventType)) {
            return this.getCfg().get(eventType);
        }
        
        return Collections.emptyList();
    }
    
    public void addSubscriber(String eventType, BaseSubscriber sub) {
        if (this.getCfg().containsKey(eventType)) {
            this.getCfg().get(eventType).add(sub);
        } else {
            List<BaseSubscriber> list = new LinkedList<>();
            list.add(sub);
            this.getCfg().put(eventType, list);
        }
    }
}
