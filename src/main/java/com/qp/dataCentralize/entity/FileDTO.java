package com.qp.dataCentralize.entity;

import lombok.Data;

import java.time.Instant;


@Data
public class FileDTO {
    private int id;
    private String type;
    private String fileName;
    private String fileLink;
    private String fileSize;
    private Instant time;
    private String createdBy;
    private String location;

    public FileDTO(int id, String type, String fileName, String fileLink, String fileSize, Instant time, String createdBy) {
        this.id = id;
        this.type = type;
        this.fileName = fileName;
        this.fileLink = fileLink;
        this.fileSize = fileSize;
        this.time = time;
        this.createdBy = createdBy;
    }
}
