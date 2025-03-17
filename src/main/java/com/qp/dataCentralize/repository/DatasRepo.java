package com.qp.dataCentralize.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.qp.dataCentralize.entity.Datas;

@Repository
public interface DatasRepo extends JpaRepository<Datas, Integer> {
	Page<Datas> findAllByOrderByEntryDateDesc(Pageable pageable);
	Page<Datas> findAllByCategoryOrderByEntryDateDesc(String category, Pageable pageable);
}
