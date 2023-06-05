package com.auyeung.sentinel.service.impl;

import com.auyeung.sentinel.entity.Order;
import com.auyeung.sentinel.mapper.OrderMapper;
import com.auyeung.sentinel.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Date;

@AllArgsConstructor
@Service
public class OrderServiceImpl implements OrderService {

    private final OrderMapper orderMapper;

    @Override
    public int createOrder(Integer userId) {
        Order order = new Order();
        order.setUserId(userId);
        order.setTime(new Date());
        int insert = orderMapper.insert(order);
        //测试熔断
//        int i = insert / 0;
        return insert;
    }
}
