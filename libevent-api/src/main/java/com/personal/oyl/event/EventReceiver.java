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
    private EventMapper eventMapper;

    private static final int TRY_TIMES = 2;

    public EventReceiver(EventMapper eventMapper) {
        this.eventMapper = eventMapper;
    }

    public void onEvent(Event event) {
        if (null != event.getEventType()) {
            List<EventSubscriber> subs = SubscriberConfig.instance().getSubscribers(event.getEventType());
            if (null != subs && !subs.isEmpty()) {
                for (EventSubscriber sub : subs) {
                    int i = 0;
                    while (true) {
                        try {
                            sub.onEvent(event);
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
                }
            }
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
            ex.printStackTrace();
        }

        return e.getMessage();
    }

}
