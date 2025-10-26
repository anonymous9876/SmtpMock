package com.example.smtpmock.smtp;

import com.example.smtpmock.model.EmailAttachment;
import com.example.smtpmock.model.StoredEmail;
import com.example.smtpmock.service.EmailStoreService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
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
        ParsedEmailContent parsedEmailContent = extractContent(message);
        String subject = message.getSubject();
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
                parsedEmailContent.body,
                Instant.now(),
                raw,
                parsedEmailContent.attachments);
    }

    private List<String> addressesToStrings(Address[] addresses) {
        if (addresses == null) {
            return Collections.emptyList();
        }
        return Arrays.stream(addresses)
                .map(Address::toString)
                .collect(Collectors.toList());
    }

    private ParsedEmailContent extractContent(Part part) throws MessagingException, IOException {
        ParsedEmailContent result = new ParsedEmailContent();
        parsePart(part, result);
        if (result.body == null) {
            result.body = "";
        }
        return result;
    }

    private void parsePart(Part part, ParsedEmailContent result) throws MessagingException, IOException {
        Object content = part.getContent();
        if (content instanceof MimeMultipart) {
            MimeMultipart multipart = (MimeMultipart) content;
            for (int i = 0; i < multipart.getCount(); i++) {
                parsePart(multipart.getBodyPart(i), result);
            }
            return;
        }

        String disposition = part.getDisposition();
        String fileName = part.getFileName();
        boolean isAttachment = disposition != null && Part.ATTACHMENT.equalsIgnoreCase(disposition);
        if (!isAttachment) {
            isAttachment = disposition != null && Part.INLINE.equalsIgnoreCase(disposition) && fileName != null;
        }

        if (isAttachment || (fileName != null && !part.isMimeType("text/plain") && !part.isMimeType("text/html"))) {
            byte[] data = toByteArray(part.getInputStream());
            result.attachments.add(new EmailAttachment(UUID.randomUUID(),
                    fileName != null ? fileName : "attachment-" + (result.attachments.size() + 1),
                    part.getContentType(),
                    data.length,
                    data));
            return;
        }

        if (part.isMimeType("text/plain")) {
            if (result.body == null || result.body.isBlank()) {
                result.body = content != null ? content.toString() : "";
            }
            return;
        }

        if (part.isMimeType("text/html") && (result.body == null || result.body.isBlank())) {
            result.body = content != null ? content.toString() : "";
            return;
        }

        if (content instanceof String && (result.body == null || result.body.isBlank())) {
            result.body = (String) content;
        } else if (!(content instanceof String)) {
            byte[] data = toByteArray(part.getInputStream());
            result.attachments.add(new EmailAttachment(UUID.randomUUID(),
                    fileName != null ? fileName : "attachment-" + (result.attachments.size() + 1),
                    part.getContentType(),
                    data.length,
                    data));
        }
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
    private static class ParsedEmailContent {
        String body = "";
        List<EmailAttachment> attachments = new ArrayList<>();
    }
}
