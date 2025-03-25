package com.qp.dataCentralize.entity;

import java.time.Instant;
import java.util.List;

import javax.persistence.CascadeType;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;

import lombok.Data;

@Entity
@Data
public class FolderEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int entityId;
	private String folderName;
	private Instant time;
	private String createdBy;
	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
	List<FileEntity> files;
}
