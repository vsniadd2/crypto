package com.difbriy.web.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.messaging.simp.SimpMessageSendingOperations;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.messaging.SessionConnectedEvent;
import org.springframework.web.socket.messaging.SessionDisconnectEvent;

@Component
@Slf4j
public class WebSocketEventListener {

    private final SimpMessageSendingOperations messagingTemplate;

    public WebSocketEventListener(SimpMessageSendingOperations messagingTemplate) {
        this.messagingTemplate = messagingTemplate;
    }

    @EventListener
    public void handleWebSocketConnectListener(SessionConnectedEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket client connected successfully: sessionId = {}", sessionId);
        
               messagingTemplate.convertAndSendToUser(
            sessionId, 
            "/topic/connection-status", 
            new ConnectionStatusMessage("CONNECTED", "WebSocket connection established successfully", sessionId)
        );
    }

    @EventListener
    public void handleWebSocketDisconnectListener(SessionDisconnectEvent event) {
        StompHeaderAccessor headerAccessor = StompHeaderAccessor.wrap(event.getMessage());
        String sessionId = headerAccessor.getSessionId();
        
        log.info("WebSocket client disconnected: sessionId = {}", sessionId);

        messagingTemplate.convertAndSend(
            "/topic/disconnection", 
            new ConnectionStatusMessage("DISCONNECTED", "Client disconnected", sessionId)
        );
    }

    public static class ConnectionStatusMessage {
        private String status;
        private String message;
        private String sessionId;
        private long timestamp;

        public ConnectionStatusMessage(String status, String message, String sessionId) {
            this.status = status;
            this.message = message;
            this.sessionId = sessionId;
            this.timestamp = System.currentTimeMillis();
        }

        public String getStatus() { return status; }
        public String getMessage() { return message; }
        public String getSessionId() { return sessionId; }
        public long getTimestamp() { return timestamp; }

        public void setStatus(String status) { this.status = status; }
        public void setMessage(String message) { this.message = message; }
        public void setSessionId(String sessionId) { this.sessionId = sessionId; }
        public void setTimestamp(long timestamp) { this.timestamp = timestamp; }
    }
}