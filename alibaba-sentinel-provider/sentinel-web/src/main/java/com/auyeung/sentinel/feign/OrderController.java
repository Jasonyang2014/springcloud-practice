package com.auyeung.sentinel.feign;

import com.auyeung.sentinel.api.OrderApi;
import com.auyeung.sentinel.service.OrderService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@AllArgsConstructor
@RestController
public class OrderController implements OrderApi {

    private final OrderService orderService;

    @GetMapping("/order/create")
    @Override
    public int createOrder(@RequestParam Integer userId) {
        return orderService.createOrder(userId);
    }
}
