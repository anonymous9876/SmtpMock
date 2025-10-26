package com.example.smtpmock.service;

import com.example.smtpmock.event.EmailAddedEvent;
import com.example.smtpmock.model.EmailAttachment;
import com.example.smtpmock.model.StoredEmail;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationEventPublisher;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

class EmailStoreServiceTest {

    private ApplicationEventPublisher eventPublisher;
    private EmailStoreService service;

    @BeforeEach
    void setUp() {
        eventPublisher = mock(ApplicationEventPublisher.class);
        service = new EmailStoreService(eventPublisher);
    }

    @Test
    void addEmailStoresAtBeginningAndPublishesEvent() {
        StoredEmail first = createEmail();
        StoredEmail second = createEmail();

        service.addEmail(first);
        service.addEmail(second);

        assertThat(service.findAll()).containsExactly(second, first);

        ArgumentCaptor<EmailAddedEvent> captor = ArgumentCaptor.forClass(EmailAddedEvent.class);
        verify(eventPublisher, times(2)).publishEvent(captor.capture());
        assertThat(captor.getAllValues())
                .extracting(EmailAddedEvent::getEmail)
                .containsExactly(first, second);
    }

    @Test
    void findByIdReturnsMatchingEmail() {
        StoredEmail email = createEmail();
        service.addEmail(email);

        Optional<StoredEmail> result = service.findById(email.getId());

        assertThat(result).contains(email);
    }

    @Test
    void removeDeletesEmailWithMatchingId() {
        StoredEmail email = createEmail();
        service.addEmail(email);

        service.remove(email.getId());

        assertThat(service.findAll()).isEmpty();
    }

    @Test
    void findAttachmentReturnsAttachmentWhenPresent() {
        EmailAttachment attachment = new EmailAttachment(UUID.randomUUID(), "file.txt", "text/plain", 4, new byte[] {1, 2});
        StoredEmail email = createEmail(List.of(attachment));
        service.addEmail(email);

        Optional<EmailAttachment> result = service.findAttachment(email.getId(), attachment.getId());

        assertThat(result).contains(attachment);
    }

    @Test
    void findAttachmentReturnsEmptyWhenNotFound() {
        StoredEmail email = createEmail();
        service.addEmail(email);

        Optional<EmailAttachment> result = service.findAttachment(email.getId(), UUID.randomUUID());

        assertThat(result).isEmpty();
    }

    @Test
    void findAllReturnsUnmodifiableView() {
        StoredEmail email = createEmail();
        service.addEmail(email);

        assertThatThrownBy(() -> service.findAll().add(createEmail()))
                .isInstanceOf(UnsupportedOperationException.class);
    }

    @Test
    void clearRemovesAllEmails() {
        service.addEmail(createEmail());
        service.addEmail(createEmail());

        service.clear();

        assertThat(service.findAll()).isEmpty();
    }

    private StoredEmail createEmail() {
        return createEmail(List.of());
    }

    private StoredEmail createEmail(List<EmailAttachment> attachments) {
        return new StoredEmail(
                UUID.randomUUID(),
                "sender@example.com",
                List.of("recipient@example.com"),
                List.of(),
                List.of(),
                "Subject",
                "Body",
                Instant.now(),
                "RAW",
                attachments
        );
    }
}
