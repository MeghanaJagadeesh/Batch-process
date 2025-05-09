package com.qp.dataCentralize.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class FavoriteFolders {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private int entityId;
	private String type;
}
