package org.processon.springbootwebsocket;

import jakarta.websocket.*;
import jakarta.websocket.server.ServerEndpoint;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicInteger;

@Slf4j
@Service
@ServerEndpoint("/websocket/ws")
public class MsgWebSocket {

    private static final  ConcurrentHashMap<String, Session> sessions = new ConcurrentHashMap<>();

    private static final AtomicInteger onlineCount = new AtomicInteger(0);

    /**
     * 建立连接调用的方法
     */
    @OnOpen
    public void onOpen(Session session) {
        String userId = session.getRequestParameterMap().get("userId").get(0);
        // 加入Set中
        sessions.put(userId,session);
        // 在线数增加
        onlineCount.getAndIncrement();
        log.info("session-{},online-count-{}",session.getId(),onlineCount.get());
    }

    /**
     * 客户端消息处理的方法
     */
    @OnMessage
    public void sendMsg(Session sender,String message) throws Exception {
        log.info("session-{},receive-msg-{}",sender.getId(),message);
        for (Session session : sessions.values()) {
            if (session.isOpen()) {
                session.getBasicRemote().sendText(message);
            }
        }

    }

    /**
     * 关闭连接调用的方法
     */
    @OnClose
    public void onClose(Session session) {
        String userId = session.getRequestParameterMap().get("userId").get(0);
        // 从Set中删除
        sessions.remove(userId);
        // 在线数减少
        onlineCount.getAndDecrement();
        log.info("session-{},down-line-count-{}",session.getId(),onlineCount.get());
    }

    /**
     * 发生错误调用的方法
     */
    @OnError
    public void onError(Session session, Throwable throwable) throws Exception {
        log.error("Web Stock Error", throwable);
        session.getBasicRemote().sendText(throwable.getMessage());
    }
}
