package com.personal.oyl.event;

import java.util.List;

/**
 * @author OuYang Liang
 */
public interface EventMapper {
    void insert(int tbNum, Event event);
    
    List<Event> queryTopN(int tbNum, int limit);
    
    void batchClean(int tbNum, List<String> eventIds);

    void fail(String subscriberId, Event event, String error);

    int numberOfFailed(String subscriberId, long routeKey, String eventType);

    boolean isDuplicated(String eventId, String subId);

    void markProcessed(String eventId, String subId);

    List<FailedEvent> queryFailedEvent(int limit);

    void markReprocessed(long failedEventId);
}
