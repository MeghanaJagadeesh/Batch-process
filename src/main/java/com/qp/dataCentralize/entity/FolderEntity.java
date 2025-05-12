package com.qp.dataCentralize.entity;

import java.time.Instant;
import java.util.List;

import javax.persistence.*;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
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

	@OneToMany(cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	private List<FileEntity> files;

	@OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
	@JsonManagedReference
	private List<FolderEntity> subFolder;

	@ManyToOne
	@JsonBackReference
	private FolderEntity parent;
}
