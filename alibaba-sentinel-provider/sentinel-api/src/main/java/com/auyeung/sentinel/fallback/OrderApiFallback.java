package com.auyeung.sentinel.fallback;

import com.auyeung.sentinel.api.OrderApi;
import com.auyeung.sentinel.exception.SystemException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class OrderApiFallback implements OrderApi {
    @Override
    public int createOrder(Integer userId){
        //默认失败0
        log.error("call order api error");
//        if (RootContext.inGlobalTransaction()) {
//            try {
//                String xid = RootContext.getXID();
//                GlobalTransaction globalTransaction = GlobalTransactionContext.getCurrent();
//                GlobalStatus status = globalTransaction.getStatus();
//                log.error("remote call error, rollback transaction {} status {}", xid, status);
//                GlobalTransactionContext.reload(xid).rollback();
//            } catch (TransactionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//        return 0;
        throw new SystemException("api error");
    }
}
