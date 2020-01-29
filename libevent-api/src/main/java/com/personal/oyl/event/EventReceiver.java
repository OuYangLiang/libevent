package com.personal.oyl.event;

import java.util.List;

/**
 * @author OuYang Liang
 */
public final class EventReceiver {
    private static volatile EventReceiver instance;

    private EventReceiver () {

    }

    public static EventReceiver instance() {
        if (null == instance) {
            synchronized (EventReceiver.class) {
                if (null == instance) {
                    instance = new EventReceiver();
                }
            }
        }

        return instance;
    }

    public void onEvent(Event event) {
        if (null != event.getEventType()) {
            List<EventSubscriber> subs = SubscriberConfig.instance().getSubscribers(event.getEventType());
            if (null != subs && !subs.isEmpty()) {
                for (EventSubscriber sub : subs) {
                    try {
                        sub.onEvent(event);
                    } catch (Exception e) {
                        // should not throw any exception under EventSubscriber#onEvent
                    }
                }
            }
        }
    }
}
