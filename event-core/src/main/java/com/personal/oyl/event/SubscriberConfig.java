package com.personal.oyl.event;

import java.util.Collections;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public final class SubscriberConfig {
    private static SubscriberConfig instance;
    
    private SubscriberConfig () {
        
    }
    
    public static SubscriberConfig instance() {
        if (null == instance) {
            synchronized (SubscriberConfig.class) {
                if (null == instance) {
                    instance = new SubscriberConfig();
                }
            }
        }
        
        return instance;
    }
    
    
    private Map<String, List<BaseSubscriber>> cfg = new ConcurrentHashMap<>();

    public List<BaseSubscriber> getSubscribers(String eventType) {
        if (this.cfg.containsKey(eventType)) {
            return this.cfg.get(eventType);
        }
        
        return Collections.emptyList();
    }
    
    public void addSubscriber(String eventType, BaseSubscriber sub) {
        if (this.cfg.containsKey(eventType)) {
            this.cfg.get(eventType).add(sub);
        } else {
            List<BaseSubscriber> list = new LinkedList<>();
            list.add(sub);
            this.cfg.put(eventType, list);
        }
    }
}
