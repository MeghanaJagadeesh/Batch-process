package com.qp.dataCentralize.entity;

import java.time.Instant;

public class FolderDTO {
	private int entityId;
    private String folderName;
    private Instant time;
    private String createdBy;

    public FolderDTO(int entityId, String folderName, Instant time, String createdBy) {
        this.entityId = entityId;
        this.folderName = folderName;
        this.time = time;
        this.createdBy = createdBy;
    }

    public int getEntityId() { return entityId; }
    public String getFolderName() { return folderName; }
    public Instant getTime() { return time; }
    public String getCreatedBy() { return createdBy; }
}