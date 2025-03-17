package com.qp.dataCentralize.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Component;

import com.qp.dataCentralize.entity.Datas;
import com.qp.dataCentralize.repository.DatasRepo;

@Component
public class DatasDao {

	@Autowired
	DatasRepo dataRepository;

	public Page<Datas> getPaginatedData(int pageNo, int pageSize) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return dataRepository.findAllByOrderByEntryDateDesc(pageable);
	}

	public Page<Datas> getPaginatedData(int pageNo, int pageSize, String category) {
		Pageable pageable = PageRequest.of(pageNo, pageSize);
		return dataRepository.findAllByCategoryOrderByEntryDateDesc(category, pageable);
	}
}
