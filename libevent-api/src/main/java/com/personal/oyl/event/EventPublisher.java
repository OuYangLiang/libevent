package com.personal.oyl.event;

import java.util.Date;

/**
 * @author OuYang Liang
 */
public class EventPublisher {
    
    private EventMapper mapper;
    
    public EventPublisher(EventMapper mapper) {
        this.mapper = mapper;
    }
    
    public void publish(int tbNum, Event event) {
        this.mapper.insert(tbNum, event);
    }
    
    public void publish(String eventType, Date eventTime, String context, int tbNum) {
        this.mapper.insert(tbNum, new Event(eventType, eventTime, context));
    }
}
