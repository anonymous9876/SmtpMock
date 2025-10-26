package com.example.smtpmock.controller;

import com.example.smtpmock.model.EmailAttachment;
import com.example.smtpmock.model.StoredEmail;
import com.example.smtpmock.service.EmailStoreService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(EmailController.class)
class EmailControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private EmailStoreService emailStoreService;

    @Test
    void findAllReturnsListOfEmails() throws Exception {
        StoredEmail email = createEmail();
        given(emailStoreService.findAll()).willReturn(List.of(email));

        mockMvc.perform(get("/api/emails"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(email.getId().toString()))
                .andExpect(jsonPath("$[0].subject").value(email.getSubject()));
    }

    @Test
    void findByIdReturnsEmailWhenPresent() throws Exception {
        StoredEmail email = createEmail();
        given(emailStoreService.findById(email.getId())).willReturn(Optional.of(email));

        mockMvc.perform(get("/api/emails/{id}", email.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(email.getId().toString()))
                .andExpect(jsonPath("$.from").value(email.getFrom()));
    }

    @Test
    void findByIdReturnsNotFoundWhenMissing() throws Exception {
        UUID id = UUID.randomUUID();
        given(emailStoreService.findById(id)).willReturn(Optional.empty());

        mockMvc.perform(get("/api/emails/{id}", id))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteAllClearsStore() throws Exception {
        mockMvc.perform(delete("/api/emails"))
                .andExpect(status().isNoContent());

        verify(emailStoreService).clear();
    }

    @Nested
    @DisplayName("Delete single email")
    class DeleteSingleEmail {

        @Test
        void removesEmailWhenPresent() throws Exception {
            StoredEmail email = createEmail();
            given(emailStoreService.findById(email.getId())).willReturn(Optional.of(email));

            mockMvc.perform(delete("/api/emails/{id}", email.getId()))
                    .andExpect(status().isNoContent());

            verify(emailStoreService).remove(email.getId());
        }

        @Test
        void returnsNotFoundWhenEmailMissing() throws Exception {
            UUID id = UUID.randomUUID();
            given(emailStoreService.findById(id)).willReturn(Optional.empty());

            mockMvc.perform(delete("/api/emails/{id}", id))
                    .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("Download attachment")
    class DownloadAttachment {

        @Test
        void returnsAttachmentWithSanitizedFilename() throws Exception {
            UUID emailId = UUID.randomUUID();
            UUID attachmentId = UUID.randomUUID();
            EmailAttachment attachment = new EmailAttachment(attachmentId, "bad\"\nname", "text/plain", 4, "test".getBytes());
            given(emailStoreService.findAttachment(emailId, attachmentId)).willReturn(Optional.of(attachment));

            mockMvc.perform(get("/api/emails/{emailId}/attachments/{attachmentId}", emailId, attachmentId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"bad__name\""))
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.TEXT_PLAIN_VALUE))
                    .andExpect(header().longValue(HttpHeaders.CONTENT_LENGTH, attachment.getSize()))
                    .andExpect(content().bytes(attachment.getData()));
        }

        @Test
        void fallsBackToOctetStreamOnInvalidContentType() throws Exception {
            UUID emailId = UUID.randomUUID();
            UUID attachmentId = UUID.randomUUID();
            EmailAttachment attachment = new EmailAttachment(attachmentId, "file.txt", "???", 2, new byte[] {1, 2});
            given(emailStoreService.findAttachment(emailId, attachmentId)).willReturn(Optional.of(attachment));

            mockMvc.perform(get("/api/emails/{emailId}/attachments/{attachmentId}", emailId, attachmentId))
                    .andExpect(status().isOk())
                    .andExpect(header().string(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_OCTET_STREAM_VALUE))
                    .andExpect(content().bytes(attachment.getData()));
        }

        @Test
        void returnsNotFoundWhenAttachmentMissing() throws Exception {
            UUID emailId = UUID.randomUUID();
            UUID attachmentId = UUID.randomUUID();
            given(emailStoreService.findAttachment(emailId, attachmentId)).willReturn(Optional.empty());

            mockMvc.perform(get("/api/emails/{emailId}/attachments/{attachmentId}", emailId, attachmentId))
                    .andExpect(status().isNotFound());
        }
    }

    private StoredEmail createEmail() {
        return new StoredEmail(
                UUID.randomUUID(),
                "sender@example.com",
                List.of("recipient@example.com"),
                List.of(),
                List.of(),
                "Subject",
                "Body",
                Instant.parse("2023-01-01T10:15:30Z"),
                "RAW",
                List.of(new EmailAttachment(UUID.randomUUID(), "file.txt", "text/plain", 4, new byte[] {1, 2, 3}))
        );
    }
}
