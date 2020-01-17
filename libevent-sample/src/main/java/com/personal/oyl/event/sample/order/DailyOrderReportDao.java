package com.personal.oyl.event.sample.order;

import java.sql.Date;
import java.util.List;

/**
 * @author OuYang Liang
 */
public interface DailyOrderReportDao {
    void insert(DailyOrderReport report);
    
    void update(DailyOrderReport report);
    
    DailyOrderReport selectByKey(Date day);
    
    List<DailyOrderReport> selectAll();
}
