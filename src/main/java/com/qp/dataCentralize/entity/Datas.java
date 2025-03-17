package com.qp.dataCentralize.entity;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

import lombok.Data;

@Entity
@Data
public class Datas {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private int id;
	private String name;
	private String email;
	private String phoneNumber;
	private String category;
	private String designation;
	private String address;
	private String companyName;
	private String industryType;
	private String entryDate;
	private String enteredBy;

}
