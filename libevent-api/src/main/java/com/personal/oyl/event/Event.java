package com.personal.oyl.event;

import java.util.Date;
import java.util.UUID;

/**
 * @author OuYang Liang
 */
public class Event {
    private String eventId;
    private String eventType;
    private Date eventTime;
    private String context;
    private long routeKey;

    public Event() {
        super();
    }

    public Event(String eventType, Date eventTime, String context, long routeKey) {
        super();
        this.eventType = eventType;
        this.eventTime = new Date(eventTime.getTime());
        this.context = context;
        this.eventId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
        this.routeKey = routeKey;
    }

    public String getEventId() {
        return eventId;
    }

    public void setEventId(String eventId) {
        this.eventId = eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getEventTime() {
        return new Date(eventTime.getTime());
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = new Date(eventTime.getTime());
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public long getRouteKey() {
        return routeKey;
    }

    public void setRouteKey(long routeKey) {
        this.routeKey = routeKey;
    }
}
