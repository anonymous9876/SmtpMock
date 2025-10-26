package com.example.smtpmock.controller;

import com.example.smtpmock.model.StoredEmail;
import com.example.smtpmock.service.EmailStoreService;
import org.springframework.http.HttpStatus;
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
}
