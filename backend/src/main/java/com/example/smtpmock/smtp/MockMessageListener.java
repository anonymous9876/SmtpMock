package com.example.smtpmock.smtp;

import com.example.smtpmock.model.StoredEmail;
import com.example.smtpmock.service.EmailStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Properties;
import java.util.UUID;
import java.util.stream.Collectors;

import org.subethamail.smtp.helper.SimpleMessageListener;

@Component
public class MockMessageListener implements SimpleMessageListener {

    private static final Logger LOGGER = LoggerFactory.getLogger(MockMessageListener.class);

    private final EmailStoreService emailStoreService;

    public MockMessageListener(EmailStoreService emailStoreService) {
        this.emailStoreService = emailStoreService;
    }

    @Override
    public boolean accept(String from, String recipient) {
        return true;
    }

    @Override
    public void deliver(String from, String recipient, InputStream data) {
        try {
            byte[] rawBytes = toByteArray(data);
            MimeMessage message = new MimeMessage(Session.getDefaultInstance(new Properties()), new ByteArrayInputStream(rawBytes));
            StoredEmail email = toStoredEmail(message, rawBytes);
            emailStoreService.addEmail(email);
            LOGGER.info("Captured email from {} with subject {}", email.getFrom(), email.getSubject());
        } catch (MessagingException | IOException e) {
            LOGGER.error("Failed to process incoming email", e);
        }
    }

    private StoredEmail toStoredEmail(MimeMessage message, byte[] rawBytes) throws MessagingException, IOException {
        String subject = message.getSubject();
        String body = extractBody(message);
        List<String> to = addressesToStrings(message.getRecipients(Message.RecipientType.TO));
        List<String> cc = addressesToStrings(message.getRecipients(Message.RecipientType.CC));
        List<String> bcc = addressesToStrings(message.getRecipients(Message.RecipientType.BCC));
        String raw = new String(rawBytes, StandardCharsets.UTF_8);
        return new StoredEmail(UUID.randomUUID(),
                message.getFrom() != null && message.getFrom().length > 0 ? message.getFrom()[0].toString() : null,
                to,
                cc,
                bcc,
                subject,
                body,
                Instant.now(),
                raw);
    }

    private List<String> addressesToStrings(Address[] addresses) {
        if (addresses == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(addresses)
                .map(Address::toString)
                .collect(Collectors.toList());
    }

    private String extractBody(MimeMessage message) throws IOException, MessagingException {
        Object content = message.getContent();
        if (content instanceof String) {
            return (String) content;
        }
        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            StringBuilder builder = new StringBuilder();
            for (int i = 0; i < multipart.getCount(); i++) {
                Object partContent = multipart.getBodyPart(i).getContent();
                builder.append(partContent.toString());
                if (i < multipart.getCount() - 1) {
                    builder.append("\n");
                }
            }
            return builder.toString();
        }
        return content != null ? content.toString() : "";
    }

    private byte[] toByteArray(InputStream inputStream) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        byte[] buffer = new byte[4096];
        int length;
        while ((length = inputStream.read(buffer)) != -1) {
            outputStream.write(buffer, 0, length);
        }
        return outputStream.toByteArray();
    }
}
