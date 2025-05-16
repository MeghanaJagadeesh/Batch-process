package com.qp.dataCentralize.repository;

import com.qp.dataCentralize.entity.FolderDTO;
import com.qp.dataCentralize.entity.FolderEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FolderRepository extends JpaRepository<FolderEntity, Integer> {

    @Query("SELECT f FROM FolderEntity f JOIN f.files file WHERE file.id = :fileId")
    Optional<FolderEntity> findFolderByFileId(@Param("fileId") int fileId);

    Optional<FolderEntity> findByFolderNameAndParent(String folderName, FolderEntity parent);

    @Query("SELECT COUNT(f) > 0 FROM FolderEntity f WHERE f.folderName = :folderName AND f.createdBy LIKE %:searchPattern% AND f.parent IS NULL")
    boolean existsByFolderNameAndDepartment(@Param("folderName") String folderName, @Param("searchPattern") String searchPattern);

    @Query("SELECT new com.qp.dataCentralize.entity.FolderDTO(" +
            "f.entityId, f.folderName, f.time, f.createdBy) " +
            "FROM FolderEntity f " +
            "WHERE f.createdBy LIKE %:searchPattern% AND f.parent IS NULL")
    Page<FolderDTO> findRootFoldersByDepartment(@Param("searchPattern") String searchPattern, Pageable pageable);

    @Query("SELECT new com.qp.dataCentralize.entity.FolderDTO(f.entityId, f.folderName, f.time, f.createdBy) " +
            "FROM FolderEntity f WHERE LOWER(f.folderName) LIKE LOWER(CONCAT('%', :searchText, '%'))")
    List<FolderDTO> searchFolders(@Param("searchText") String searchText);

    @Query("SELECT new com.qp.dataCentralize.entity.FolderDTO(" +
            "f.entityId, f.folderName, f.time, f.createdBy) " +
            "FROM FolderEntity f " +
            "WHERE f.createdBy LIKE %:searchPattern%")
    Page<FolderDTO> findFolderNamesAndIds(@Param("searchPattern") String searchPattern, Pageable pageable);

    @Query("SELECT f FROM FolderEntity f LEFT JOIN FETCH f.files WHERE f.entityId = :folderId")
    FolderEntity findFolderWithFiles(@Param("folderId") int folderId);

    @Query("SELECT f.subFolder FROM FolderEntity f WHERE f.entityId = :folderId")
    List<FolderEntity> findSubFolders(@Param("folderId") int folderId);

    @Query("SELECT NEW com.qp.dataCentralize.entity.FolderDTO(f.entityId, f.folderName, f.time, f.createdBy) " +
            "FROM FolderEntity f WHERE f.entityId IN :folderIds")
    List<FolderDTO> findFoldersByIds(@Param("folderIds") List<Integer> folderIds);
}

