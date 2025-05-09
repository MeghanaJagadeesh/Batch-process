package com.qp.dataCentralize.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.FileEntity;

import java.util.Optional;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {

	
//	public FileEntity findByFileLink(String url);
//	public FileEntity findByFileName(String filename);

	@Query("SELECT f FROM FolderEntity fe JOIN fe.files f WHERE fe.entityId = :folderId AND f.fileName = :fileName")
	Optional<FileEntity> findFileInFolderByFileName(@Param("folderId") int folderId, @Param("fileName") String fileName);

	// 🔍 Find file by folder ID and file link
	@Query("SELECT f FROM FolderEntity fe JOIN fe.files f WHERE fe.entityId = :folderId AND f.fileLink = :fileLink")
	Optional<FileEntity> findFileInFolderByFileLink(@Param("folderId") int folderId, @Param("fileLink") String fileLink);

}

