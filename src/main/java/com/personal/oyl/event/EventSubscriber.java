package com.personal.oyl.event;

public interface EventSubscriber {
    void onEvent(Event e);
    
    String id();
}
