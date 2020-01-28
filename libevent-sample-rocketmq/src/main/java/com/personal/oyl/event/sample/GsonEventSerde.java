package com.personal.oyl.event.sample;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventSerde;

/**
 * @author OuYang Liang
 */
public class GsonEventSerde implements EventSerde {

    private static final Gson gson = new GsonBuilder().setDateFormat("yyyy-MM-dd HH:mm:ss").create();

    @Override
    public Event fromJson(String json) {
        return gson.fromJson(json, Event.class);
    }

    @Override
    public String toJson(Event event) {
        return gson.toJson(event);
    }
}
