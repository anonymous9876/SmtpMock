package com.example.smtpmock.event;

import com.example.smtpmock.model.StoredEmail;

public class EmailAddedEvent {

    private final StoredEmail email;

    public EmailAddedEvent(StoredEmail email) {
        this.email = email;
    }

    public StoredEmail getEmail() {
        return email;
    }
}
