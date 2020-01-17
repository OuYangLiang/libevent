package com.personal.oyl.event;

/**
 * @author OuYang Liang
 */
public interface EventSerde {
    Event fromJson(String json);

    String toJson(Event event);
}
