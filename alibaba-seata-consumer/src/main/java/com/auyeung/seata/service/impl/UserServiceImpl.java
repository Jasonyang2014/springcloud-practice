package com.auyeung.seata.service.impl;

import com.auyeung.seata.entity.User;
import com.auyeung.seata.mapper.UserMapper;
import com.auyeung.seata.service.UserService;
import com.auyeung.sentinel.api.OrderApi;
import io.seata.spring.annotation.GlobalTransactional;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@AllArgsConstructor
@Service
public class UserServiceImpl implements UserService {

    private final UserMapper userMapper;

    private final OrderApi orderApi;

    /**
     * 转账
     *
     * @param sId     源用户id
     * @param tId     目标用户id
     * @param balance 金额
     * @return 是否成功
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public boolean transfer(Integer sId, Integer tId, BigDecimal balance) {
        boolean s = userMapper.addBalance(tId, balance);
        boolean t = userMapper.subBalance(sId, balance);
        return s && t;
    }

    /**
     * 根据id获取用户
     *
     * @param id id
     * @return
     */
    @Override
    public User getUser(Integer id) {
        return userMapper.selectById(id);
    }

    /**
     * 创建订单
     *
     * @param id 用户id
     * @return
     */
    @GlobalTransactional(rollbackFor = Exception.class)
    @Override
    public boolean createOrder(Integer id) {
        boolean b = userMapper.subBalance(id, BigDecimal.ONE);
        if (b) {
            int order = orderApi.createOrder(id);
            return order == 1;
        } else {
            return false;
        }
    }


}
