package com.qp.dataCentralize.entity;

import lombok.Data;

import java.time.Instant;
import java.util.List;

@Data
public class FolderWithFilesDTO {
    private int entityId;
    private String folderName;
    private Instant time;
    private String createdBy;
    private List<FileEntity> files;
    private List<FolderDTO> subFolder;

    public FolderWithFilesDTO(int entityId, String folderName, Instant time, String createdBy,
                              List<FileEntity> files, List<FolderDTO> subFolder) {
        this.entityId = entityId;
        this.folderName = folderName;
        this.time = time;
        this.createdBy = createdBy;
        this.files = files;
        this.subFolder = subFolder;
    }

    public int getEntityId() { return entityId; }
    public String getFolderName() { return folderName; }
    public Instant getTime() { return time; }
    public String getCreatedBy() { return createdBy; }
    public List<FileEntity> getFiles() { return files; }
    public List<FolderDTO> getSubFolder() { return subFolder; }

}
