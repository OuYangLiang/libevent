package com.personal.oyl.event;

import java.util.List;
import java.util.Map;

public interface EventMapper {
    void insert(Event event);
    
    List<Event> queryTopN(int tbNum, int limit);
    
    void batchClean(int tbNum, List<Long> ids);
}
