package com.personal.oyl.event;

import java.util.LinkedList;
import java.util.List;

/**
 * @author OuYang Liang
 */
public class DefaultEventPusher implements EventPusher {
    @Override
    public List<String> push(int tbNum, List<Event> events) {
        List<String> eventIds = new LinkedList<>();

        for (Event event : events) {
            EventReceiver.instance().onEvent(event);
            eventIds.add(event.getEventId());
        }

        return eventIds;
    }
}
