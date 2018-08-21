package com.personal.oyl.event.web.subscribers;

import com.personal.oyl.event.BaseSubscriber;
import com.personal.oyl.event.Event;

public class Sub1 implements BaseSubscriber {

    @Override
    public void onEvent(Event e) {
        System.out.println(e.json());
    }

}
