package com.qp.dataCentralize.repository;

import com.qp.dataCentralize.entity.FileDTO;
import com.qp.dataCentralize.entity.FileEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {

    @Query("SELECT new com.qp.dataCentralize.entity.FileDTO(f.id, f.type, f.fileName, f.fileLink, f.fileSize, f.time, f.createdBy) " +
            "FROM FileEntity f WHERE LOWER(f.fileName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<FileDTO> searchFiles(@Param("searchText") String searchText);

    @Query("SELECT f FROM FolderEntity fe JOIN fe.files f WHERE fe.entityId = :folderId AND f.fileName = :fileName")
    Optional<FileEntity> findFileInFolderByFileName(@Param("folderId") int folderId, @Param("fileName") String fileName);

    @Query("SELECT f FROM FolderEntity fe JOIN fe.files f WHERE fe.entityId = :folderId AND f.fileLink = :fileLink")
    Optional<FileEntity> findFileInFolderByFileLink(@Param("folderId") int folderId, @Param("fileLink") String fileLink);
}

