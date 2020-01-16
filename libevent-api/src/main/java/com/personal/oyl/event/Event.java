package com.personal.oyl.event;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import java.util.Date;
import java.util.UUID;

/**
 * @author OuYang Liang
 */
public class Event {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    private String eventId;
    private String eventType;
    private Date eventTime;
    private String context;

    public Event() {
        super();
    }

    public Event(String eventType, Date eventTime, String context) {
        super();
        this.eventType = eventType;
        this.eventTime = new Date(eventTime.getTime());
        this.context = context;
        this.eventId = UUID.randomUUID().toString().replaceAll("-", "").toUpperCase();
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

    public String json() {
        return gson.toJson(this);
    }

    public static Event fromJson(String json) {
        return gson.fromJson(json, Event.class);
    }

}
