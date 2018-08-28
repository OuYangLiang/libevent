package com.personal.oyl.event;

import java.util.List;
import java.util.Map;

public interface EventMapper {
    void insert(Event event);
    
    List<Event> queryTopN(Map<String, Object> param);
    
    void batchClean(Map<String, Object> param);
}
