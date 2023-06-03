package com.auyeung.seata.mapper;

import com.auyeung.seata.entity.User;
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;

import java.math.BigDecimal;

@Mapper
public interface UserMapper extends BaseMapper<User> {

    /**
     * 增加金额
     *
     * @param id      id
     * @param balance 金额
     * @return
     */
    boolean addBalance(@Param("id") Integer id, @Param("balance") BigDecimal balance);

    /**
     * 减少金额
     *
     * @param id      id
     * @param balance 金额
     * @return
     */
    boolean subBalance(@Param("id") Integer id, @Param("balance") BigDecimal balance);

}
