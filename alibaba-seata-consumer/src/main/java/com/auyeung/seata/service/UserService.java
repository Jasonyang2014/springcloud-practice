package com.auyeung.seata.service;

import com.auyeung.seata.entity.User;

import java.math.BigDecimal;

public interface UserService {

    /**
     * 转账
     *
     * @param sId 源用户id
     * @param tId 目标用户id
     * @return 是否成功
     */
    boolean transfer(Integer sId, Integer tId, BigDecimal balance);

    /**
     * 根据id获取用户
     *
     * @param id id
     * @return
     */
    User getUser(Integer id);

    /**
     * 创建订单
     * @param id 用户id
     * @return
     */
    boolean createOrder(Integer id);
}
