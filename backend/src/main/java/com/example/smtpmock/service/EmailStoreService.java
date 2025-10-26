package com.example.smtpmock.service;

import com.example.smtpmock.model.EmailAttachment;
import com.example.smtpmock.model.StoredEmail;
import org.springframework.stereotype.Service;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class EmailStoreService {

    private final CopyOnWriteArrayList<StoredEmail> emails = new CopyOnWriteArrayList<>();

    public List<StoredEmail> findAll() {
        return Collections.unmodifiableList(emails);
    }

    public Optional<StoredEmail> findById(UUID id) {
        return emails.stream()
                .filter(email -> email.getId().equals(id))
                .findFirst();
    }

    public StoredEmail addEmail(StoredEmail email) {
        emails.add(0, email);
        return email;
    }

    public void remove(UUID id) {
        emails.removeIf(email -> email.getId().equals(id));
    }

    public Optional<EmailAttachment> findAttachment(UUID emailId, UUID attachmentId) {
        return findById(emailId)
                .flatMap(email -> email.getAttachments().stream()
                        .filter(attachment -> attachmentId.equals(attachment.getId()))
                        .findFirst());
    }

    public void clear() {
        emails.clear();
    }
}
