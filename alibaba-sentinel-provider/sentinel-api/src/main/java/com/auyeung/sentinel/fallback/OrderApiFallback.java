package com.auyeung.sentinel.fallback;

import com.auyeung.sentinel.api.OrderApi;
import io.seata.core.context.RootContext;
import io.seata.core.exception.TransactionException;
import io.seata.tm.api.GlobalTransactionContext;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderApiFallback implements OrderApi {
    @Override
    public int createOrder(Integer userId) {
        //默认失败0
        log.error("call order api error");
        if (RootContext.inGlobalTransaction()) {
            try {
                String xid = RootContext.getXID();
                log.error("remote call error, rollback transaction {}", xid);
                GlobalTransactionContext.reload(xid).rollback();
            } catch (TransactionException e) {
                throw new RuntimeException(e);
            }
        }
        return 0;
    }
}
