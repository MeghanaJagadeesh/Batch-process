package com.qp.dataCentralize.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.FavoriteFolders;

@Repository
public interface FavoriteFolderRepository extends JpaRepository<FavoriteFolders, Integer> {

	 @Query("SELECT f FROM FavoriteFolders f WHERE f.entityId = :entityId AND f.type = :type")
	    FavoriteFolders findByEntityIdAndType(@Param("entityId") int entityId, @Param("type") String type);

}
