package com.personal.oyl.event;

import java.io.IOException;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.util.List;

/**
 * @author OuYang Liang
 */
public final class EventReceiver {
    private final EventMapper eventMapper;

    private static final int TRY_TIMES = 2;

    public EventReceiver(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public void onEvent(Event event) {
        if (null == event.getEventType()) {
            return;
        }

        List<EventSubscriber> subs = SubscriberConfig.instance.getSubscribers(event.getEventType());
        if (null != subs && !subs.isEmpty()) {
            for (EventSubscriber sub : subs) {
                if (eventMapper.isDuplicated(event.getEventId(), sub.id())) {
                    continue;
                }

                int numOfFailed = eventMapper.numberOfFailed(sub.id(), event.getRouteKey(), event.getEventType());
                if (numOfFailed == 0) {
                    int i = 0;
                    while (true) {
                        try {
                            sub.onEvent(event);
                            try {
                                eventMapper.markProcessed(event.getEventId(), sub.id());
                            } catch (Exception e) {
                                // ignore
                            }
                            break;
                        } catch (Exception e) {
                            i++;
                            if (i > TRY_TIMES) {
                                try {
                                    eventMapper.fail(sub.id(), event, toStack(e));
                                } catch (Exception ex) {
                                    // ignore
                                }
                                break;
                            }
                        }
                    }
                } else {
                    try {
                        eventMapper.fail(sub.id(), event, "delayed");
                    } catch (Exception e) {
                        // ignore
                    }
                }
            }
        }
    }

    public boolean retryEvent(FailedEvent failedEvent) {
        if (eventMapper.isDuplicated(failedEvent.getEvent().getEventId(), failedEvent.getSubscriberId())) {
            return true;
        }

        try {
            SubscriberConfig.instance.getSubscriber(failedEvent.getSubscriberId()).onEvent(failedEvent.getEvent());
            try {
                eventMapper.markProcessed(failedEvent.getEvent().getEventId(), failedEvent.getSubscriberId());
            } catch (Exception e) {
                // ignore
            }
            try {
                eventMapper.markReprocessed(failedEvent.getId());
            } catch (Exception e) {
                // ignore
            }
            return true;
        } catch (Exception e) {
            return false;
        }
    }


    private String toStack(Exception e) {
        try {
            try (
                    Writer writer = new StringWriter();
                    PrintWriter printWriter = new PrintWriter(writer)
            ) {

                e.printStackTrace(printWriter);
                return writer.toString();
            }
        } catch (IOException ex) {
            // ignore
        }

        return e.getMessage();
    }

}
