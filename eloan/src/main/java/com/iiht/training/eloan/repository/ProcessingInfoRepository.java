package com.iiht.training.eloan.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;

@Repository
public interface ProcessingInfoRepository extends JpaRepository<ProcessingInfo, Long>{

	ProcessingInfo findByLoanAppId(Long id);

}
