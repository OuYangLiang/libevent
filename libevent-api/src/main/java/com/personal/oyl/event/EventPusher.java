package com.personal.oyl.event;

import java.util.List;

/**
 * @author OuYang Liang
 */
public interface EventPusher {
    List<String> push(int tbNum, List<Event> events);
}
