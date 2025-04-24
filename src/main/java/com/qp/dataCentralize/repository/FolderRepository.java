package com.qp.dataCentralize.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.FolderDTO;
import com.qp.dataCentralize.entity.FolderEntity;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Integer> {

	public FolderEntity findByFolderName(String folderName);

	@Query("SELECT NEW com.qp.dataCentralize.entity.FolderDTO(" +
			"f.entityId, f.folderName, f.time, f.createdBy) " +
			"FROM FolderEntity f " +
			"WHERE f.createdBy LIKE %:department%")
	Page<FolderEntity> findFolderNamesAndIds(@Param("department") String department,Pageable pageable);

	@Query("SELECT f FROM FolderEntity f JOIN FETCH f.files files WHERE f.entityId = :folderId ORDER BY files.time DESC")
	FolderEntity findFolderWithFilesSortedByTime(@Param("folderId") int folderId);
	
	@Query("SELECT NEW com.qp.dataCentralize.entity.FolderDTO(f.entityId, f.folderName, f.time, f.createdBy) " +
		       "FROM FolderEntity f WHERE f.entityId IN :folderIds")
		List<FolderDTO> findFoldersByIds(@Param("folderIds") List<Integer> folderIds);
}
