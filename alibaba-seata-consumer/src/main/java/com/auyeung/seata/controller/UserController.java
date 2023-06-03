package com.auyeung.seata.controller;

import com.auyeung.seata.entity.User;
import com.auyeung.seata.service.UserService;
import lombok.AllArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.math.BigDecimal;

@AllArgsConstructor
@RequestMapping
@RestController
public class UserController {

    private final UserService userService;

    @RequestMapping("/user/{id}")
    public User getUser(@PathVariable Integer id) {
        return userService.getUser(id);
    }

    @GetMapping("/user/transfer/{sId}/{tId}/{balance}")
    public Boolean transfer(@PathVariable Integer sId,
                            @PathVariable Integer tId,
                            @PathVariable BigDecimal balance) {
        return userService.transfer(sId, tId, balance);
    }

}
