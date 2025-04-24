package com.qp.dataCentralize.entity;

import lombok.Data;

import javax.persistence.*;

@Data
@Entity
public class LeadsData {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int id;
    private String person_name;
    private String phone_number;
    private String company_name;
    private String email;
    private String address;
    private String status;
    @Column(length = 2000)
    private String note;
    private String entryDate;
    private String enteredBy;
}
