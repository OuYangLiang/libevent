package com.personal.oyl.event;

import java.util.LinkedList;
import java.util.List;

/**
 * @author OuYang Liang
 */
public class DefaultEventPusher implements EventPusher {

    private EventReceiver eventReceiver;

    public DefaultEventPusher(EventReceiver eventReceiver) {
        this.eventReceiver = eventReceiver;
    }

    @Override
    public List<String> push(int tbNum, List<Event> events) {
        List<String> eventIds = new LinkedList<>();

        for (Event event : events) {
            this.eventReceiver.onEvent(event);
            eventIds.add(event.getEventId());
        }

        return eventIds;
    }
}