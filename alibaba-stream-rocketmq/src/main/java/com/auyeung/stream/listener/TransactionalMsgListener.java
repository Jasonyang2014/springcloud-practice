package com.auyeung.stream.listener;

import lombok.extern.slf4j.Slf4j;
import org.apache.rocketmq.client.producer.LocalTransactionState;
import org.apache.rocketmq.client.producer.TransactionListener;
import org.apache.rocketmq.common.message.Message;
import org.apache.rocketmq.common.message.MessageExt;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class TransactionalMsgListener implements TransactionListener {
    /**
     * When send transactional prepare(half) message succeed, this method will be invoked to execute local transaction.
     *
     * @param msg Half(prepare) message
     * @param arg Custom business parameter
     * @return Transaction state
     */
    @Override
    public LocalTransactionState executeLocalTransaction(Message msg, Object arg) {
        byte[] body = msg.getBody();
        Object num = msg.getProperty("test");

        if ("1".equals(num)) {
            log.info("execute: " + new String(body) + " unknown");
            return LocalTransactionState.UNKNOW;
        }
        else if ("2".equals(num)) {
            log.info("execute: " + new String(body) + " rollback");
            return LocalTransactionState.ROLLBACK_MESSAGE;
        }
        log.info("execute: " + new String(body) + " commit");
        return LocalTransactionState.COMMIT_MESSAGE;
    }

    /**
     * When no response to prepare(half) message. broker will send check message to check the transaction status, and this
     * method will be invoked to get local transaction status.
     *
     * @param msg Check message
     * @return Transaction state
     */
    @Override
    public LocalTransactionState checkLocalTransaction(MessageExt msg) {
        log.info("check: " + new String(msg.getBody()));
        return LocalTransactionState.COMMIT_MESSAGE;
    }
}
