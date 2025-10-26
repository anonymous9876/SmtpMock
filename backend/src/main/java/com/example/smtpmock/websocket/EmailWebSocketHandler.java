package com.example.smtpmock.websocket;

import com.example.smtpmock.model.StoredEmail;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class EmailWebSocketHandler extends TextWebSocketHandler {

    private static final Logger LOGGER = LoggerFactory.getLogger(EmailWebSocketHandler.class);

    private final ObjectMapper objectMapper;
    private final Set<WebSocketSession> sessions = ConcurrentHashMap.newKeySet();

    public EmailWebSocketHandler(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        sessions.add(session);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        sessions.remove(session);
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        LOGGER.warn("WebSocket transport error, closing session {}", session.getId(), exception);
        sessions.remove(session);
        try {
            session.close(CloseStatus.SERVER_ERROR);
        } catch (IOException e) {
            LOGGER.debug("Unable to close errored WebSocket session", e);
        }
    }

    public void broadcastEmail(StoredEmail email) {
        String payload;
        try {
            payload = objectMapper.writeValueAsString(email);
        } catch (JsonProcessingException e) {
            LOGGER.error("Unable to serialize email {} for WebSocket clients", email.getId(), e);
            return;
        }

        TextMessage message = new TextMessage(payload);
        sessions.forEach(session -> sendMessage(session, message));
    }

    private void sendMessage(WebSocketSession session, TextMessage message) {
        if (!session.isOpen()) {
            sessions.remove(session);
            return;
        }
        try {
            session.sendMessage(message);
        } catch (IOException e) {
            LOGGER.warn("Failed to send WebSocket message to session {}", session.getId(), e);
            sessions.remove(session);
        }
    }
}
