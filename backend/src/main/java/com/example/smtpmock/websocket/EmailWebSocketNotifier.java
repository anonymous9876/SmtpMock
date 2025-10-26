package com.example.smtpmock.websocket;

import com.example.smtpmock.event.EmailAddedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

@Component
public class EmailWebSocketNotifier {

    private final EmailWebSocketHandler emailWebSocketHandler;

    public EmailWebSocketNotifier(EmailWebSocketHandler emailWebSocketHandler) {
        this.emailWebSocketHandler = emailWebSocketHandler;
    }

    @EventListener
    public void onEmailAdded(EmailAddedEvent event) {
        emailWebSocketHandler.broadcastEmail(event.getEmail());
    }
}
