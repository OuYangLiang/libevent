package com.personal.oyl.event.sample.order;

import java.sql.Date;

/**
 * @author OuYang Liang
 */
public class DailyOrderReport {
    private Long id;
    private Date day;
    private Long orderNum;
    private Long orderTotal;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Date getDay() {
        return null == day ? null : (Date) day.clone();
    }

    public void setDay(Date day) {
        this.day = (null == day ? null : (Date) day.clone());
    }

    public Long getOrderNum() {
        return orderNum;
    }

    public void setOrderNum(Long orderNum) {
        this.orderNum = orderNum;
    }

    public Long getOrderTotal() {
        return orderTotal;
    }

    public void setOrderTotal(Long orderTotal) {
        this.orderTotal = orderTotal;
    }
}
