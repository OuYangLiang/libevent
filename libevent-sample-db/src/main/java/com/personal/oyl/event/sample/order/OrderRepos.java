package com.personal.oyl.event.sample.order;

import java.sql.Date;
import java.util.List;

/**
 * @author OuYang Liang
 */
public interface OrderRepos {
    void createOrder(Order order);
    
    UserOrderReport selectUserOrderReportByKey(Long userId);
    
    List<UserOrderReport> selectAllUserReport();
    
    void createUserOrderReport(UserOrderReport report);
    
    void updateUserOrderReport(UserOrderReport report);
    
    DailyOrderReport selectDailyOrderReportByKey(Date day);
    
    List<DailyOrderReport> selectAllDailyReport();
    
    void createDailyOrderReport(DailyOrderReport report);
    
    void updateDailyOrderReport(DailyOrderReport report);
}
