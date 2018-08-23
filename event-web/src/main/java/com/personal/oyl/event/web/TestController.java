package com.personal.oyl.event.web;

import java.util.Date;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import com.personal.oyl.event.Event;
import com.personal.oyl.event.EventMapper;
import com.personal.oyl.event.EventPublisher;

@Controller
@RequestMapping("/test")
public class TestController {
    
    @Autowired
    private EventMapper mapper;
    
    @RequestMapping("/{param}")
    @ResponseBody
    public Object hello(@PathVariable String param) {
        
        EventPublisher publisher = new EventPublisher(mapper);
        
        for (int i = 0; i <= 1000; i++) {
            Event event = new Event("Event Type", new Date(), param + "_" + i, i);
            
            publisher.publish(event);
        }
        
        return "Hello " + param;
    }
}
