package com.personal.oyl.event;

import java.util.Date;

public class EventPublisher {
    
    private EventMapper mapper;
    
    public EventPublisher(EventMapper mapper) {
        this.mapper = mapper;
    }
    
    public void publish(Event event) {
        this.mapper.insert(event);
    }
    
    public void publish(String eventType, Date eventTime, String context, int group) {
        this.mapper.insert(new Event(eventType, eventTime, context, group));
    }
}
