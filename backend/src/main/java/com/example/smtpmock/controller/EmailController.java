package com.example.smtpmock.controller;

import com.example.smtpmock.model.EmailAttachment;
import com.example.smtpmock.model.StoredEmail;
import com.example.smtpmock.service.EmailStoreService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/emails")
@CrossOrigin
public class EmailController {

    private final EmailStoreService emailStoreService;

    public EmailController(EmailStoreService emailStoreService) {
        this.emailStoreService = emailStoreService;
    }

    @GetMapping
    public List<StoredEmail> findAll() {
        return emailStoreService.findAll();
    }

    @GetMapping("/{id}")
    public ResponseEntity<StoredEmail> findById(@PathVariable UUID id) {
        return emailStoreService.findById(id)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @GetMapping("/{emailId}/attachments/{attachmentId}")
    public ResponseEntity<byte[]> downloadAttachment(@PathVariable UUID emailId, @PathVariable UUID attachmentId) {
        return emailStoreService.findAttachment(emailId, attachmentId)
                .map(this::buildAttachmentResponse)
                .orElseGet(() -> ResponseEntity.notFound().build());
    }

    @DeleteMapping
    public ResponseEntity<Void> clear() {
        emailStoreService.clear();
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> remove(@PathVariable UUID id) {
        if (emailStoreService.findById(id).isPresent()) {
            emailStoreService.remove(id);
            return ResponseEntity.noContent().build();
        }
        return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
    }

    private ResponseEntity<byte[]> buildAttachmentResponse(EmailAttachment attachment) {
        MediaType mediaType = toMediaType(attachment.getContentType());
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"" + sanitizeFileName(attachment.getFileName()) + "\"")
                .contentType(mediaType)
                .contentLength(attachment.getSize())
                .body(attachment.getData());
    }

    private MediaType toMediaType(String contentType) {
        if (contentType == null || contentType.isBlank()) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
        try {
            return MediaType.parseMediaType(contentType);
        } catch (IllegalArgumentException ex) {
            return MediaType.APPLICATION_OCTET_STREAM;
        }
    }

    private String sanitizeFileName(String fileName) {
        if (fileName == null || fileName.isBlank()) {
            return "attachment";
        }
        return fileName
                .replace("\r", "_")
                .replace("\n", "_")
                .replace("\"", "_");
    }
}
