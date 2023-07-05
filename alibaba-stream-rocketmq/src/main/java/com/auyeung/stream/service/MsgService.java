package com.auyeung.stream.service;

public interface MsgService {

    int sendMsg(String msg);

    /**
     * 发送掩饰消息
     *
     * @param msg 消息
     */
    boolean sendDelayMsg(String msg);

    /**
     * 发送事务信息
     *
     * @param msg 消息
     * @return
     */
    boolean sendTxMsg(String msg);
}
