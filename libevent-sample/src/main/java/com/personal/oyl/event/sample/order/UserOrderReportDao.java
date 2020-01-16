package com.personal.oyl.event.sample.order;

import java.util.List;

/**
 * @author OuYang Liang
 */
public interface UserOrderReportDao {
    void insert(UserOrderReport report);
    
    void update(UserOrderReport report);
    
    UserOrderReport selectByKey(Long userId);
    
    List<UserOrderReport> selectAll();
}
