package com.qp.dataCentralize.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.FileEntity;

@Repository
public interface FileRepository extends JpaRepository<FileEntity, Integer> {

	
	public FileEntity findByFileLink(String url);
}

