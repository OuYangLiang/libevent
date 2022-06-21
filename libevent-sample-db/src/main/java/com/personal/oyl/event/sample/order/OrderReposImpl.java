package com.personal.oyl.event.sample.order;

import java.util.Date;
import java.util.List;

import com.personal.oyl.event.Event;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import com.personal.oyl.event.EventPublisher;

import javax.annotation.Resource;

/**
 * @author OuYang Liang
 */
@Component("orderRepos")
public class OrderReposImpl implements OrderRepos {
    
    @Resource
    private OrderDao dao;
    
    @Resource
    private EventPublisher publisher;
    
    @Resource
    private UserOrderReportDao reportDao;
    
    @Resource
    private DailyOrderReportDao dailyDao;

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void createOrder(Order order) {
        dao.insert(order);
        publisher.publish(new Event("o_c", new Date(), order.json(), order.getUserId().intValue() ));
    }

    @Override
    public UserOrderReport selectUserOrderReportByKey(Long userId) {
        return reportDao.selectByKey(userId);
    }

    @Override
    public List<UserOrderReport> selectAllUserReport() {
        return reportDao.selectAll();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void createUserOrderReport(UserOrderReport report) {
        reportDao.insert(report);
    }
    
    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void updateUserOrderReport(UserOrderReport report) {
        reportDao.update(report);
    }

    @Override
    public DailyOrderReport selectDailyOrderReportByKey(java.sql.Date day) {
        return dailyDao.selectByKey(day);
    }

    @Override
    public List<DailyOrderReport> selectAllDailyReport() {
        return dailyDao.selectAll();
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void createDailyOrderReport(DailyOrderReport report) {
        dailyDao.insert(report);
    }

    @Transactional(readOnly = false, propagation = Propagation.REQUIRED, isolation = Isolation.READ_COMMITTED, rollbackFor = Exception.class)
    @Override
    public void updateDailyOrderReport(DailyOrderReport report) {
        dailyDao.update(report);
    }

}
