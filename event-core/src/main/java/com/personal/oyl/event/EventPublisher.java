package com.personal.oyl.event;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class EventPublisher {
    
    @Autowired
    private EventMapper mapper;
    
    public void publish(Event event) {
        this.mapper.insert(event);
    }
    
    public void publish(String eventType, Date eventTime, String context, int group) {
        this.mapper.insert(new Event(eventType, eventTime, context, group));
    }
}
