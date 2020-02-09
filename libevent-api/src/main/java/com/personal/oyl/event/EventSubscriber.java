package com.personal.oyl.event;

/**
 * @author OuYang Liang
 */
public interface EventSubscriber {
    void onEvent(Event e);

    default String id() {
        return this.getClass().getCanonicalName();
    }
}
