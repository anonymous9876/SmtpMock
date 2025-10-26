package com.example.smtpmock.model;

import com.fasterxml.jackson.annotation.JsonIgnore;

import java.util.UUID;

public class EmailAttachment {

    private UUID id;
    private String fileName;
    private String contentType;
    private long size;
    private byte[] data;

    public EmailAttachment() {
    }

    public EmailAttachment(UUID id, String fileName, String contentType, long size, byte[] data) {
        this.id = id;
        this.fileName = fileName;
        this.contentType = contentType;
        this.size = size;
        this.data = data;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    public long getSize() {
        return size;
    }

    public void setSize(long size) {
        this.size = size;
    }

    @JsonIgnore
    public byte[] getData() {
        return data;
    }

    public void setData(byte[] data) {
        this.data = data;
    }
}
