package com.qp.dataCentralize.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.FolderEntity;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Integer> {

	public FolderEntity findByFolderName(String folderName);
	 @Query("SELECT entityId, folderName FROM FolderEntity ")
	    List<Object[]> findFolderNamesAndIds();
}
