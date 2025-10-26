package com.example.smtpmock.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;

@Configuration
@EnableWebSocket
public class EmailWebSocketConfig implements WebSocketConfigurer {

    private final EmailWebSocketHandler emailWebSocketHandler;

    public EmailWebSocketConfig(EmailWebSocketHandler emailWebSocketHandler) {
        this.emailWebSocketHandler = emailWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(emailWebSocketHandler, "/ws/emails")
                .setAllowedOrigins("*");
    }
}
