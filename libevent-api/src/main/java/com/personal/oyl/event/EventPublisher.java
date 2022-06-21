package com.personal.oyl.event;

/**
 * @author OuYang Liang
 */
public class EventPublisher {
    
    private final EventMapper mapper;
    private final int numOfTables;
    
    public EventPublisher(EventMapper mapper, int numOfTables) {
        this.mapper = mapper;
        this.numOfTables = numOfTables;
    }

    public void publish(Event event) {
        this.mapper.insert((int) (Math.abs(event.getRouteKey()) % numOfTables), event);
    }

}
