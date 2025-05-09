package com.qp.dataCentralize.repository;

import com.qp.dataCentralize.entity.LeadsData;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

@Repository
public interface LeadsRepository extends JpaRepository<LeadsData, Integer> , JpaSpecificationExecutor<LeadsData> {

    Page<LeadsData> findAllByOrderByEntryDateDesc(Pageable pageable);

    Page<LeadsData> findAllByStatusOrderByEntryDateDesc(String status, Pageable pageable);
}
