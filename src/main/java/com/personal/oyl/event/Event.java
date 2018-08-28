package com.personal.oyl.event;

import java.util.Date;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

public class Event {
    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();
    
    private Long id;
    private String eventType;
    private Date eventTime;
    private String context;
    private int group;

    public Event() {
        super();
    }

    public Event(String eventType, Date eventTime, String context, int group) {
        super();
        this.eventType = eventType;
        this.eventTime = eventTime;
        this.context = context;
        this.group = group;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEventType() {
        return eventType;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public Date getEventTime() {
        return eventTime;
    }

    public void setEventTime(Date eventTime) {
        this.eventTime = eventTime;
    }

    public String getContext() {
        return context;
    }

    public void setContext(String context) {
        this.context = context;
    }

    public int getGroup() {
        return group;
    }

    public void setGroup(int group) {
        this.group = group;
    }
    
    public int getTbNum() {
        return this.getGroup() % 8;
    }
    
    public String json() {
        String rlt = gson.toJson(this);
        return rlt;
    }
    
    public static Event fromJson(String json) {
        return gson.fromJson(json, Event.class);
    }
    
}
