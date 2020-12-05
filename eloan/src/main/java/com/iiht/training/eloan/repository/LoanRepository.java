package com.iiht.training.eloan.repository;

import java.util.List;
import java.util.Optional;

import javax.transaction.Transactional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.iiht.training.eloan.entity.Loan;
import com.iiht.training.eloan.entity.Users;

@Repository
public interface LoanRepository extends JpaRepository<Loan, Long>{

	List<Loan> findAllByStatus(int status);

	List<Loan> findAllByCustomerId(Long customerId);
	
	@Transactional
	@Modifying
    @Query("UPDATE Loan c SET c.status = :status WHERE c.id = :id")
    int updateStatus(@Param("id") Long loanAppId, @Param("status") int status);
	
	@Transactional
	@Modifying
    @Query("UPDATE Loan c SET c.remark = :remark WHERE c.id = :id")
    int updateRemark(@Param("id") Long loanAppId, @Param("remark") String remark);

	Users findByCustomerId(Long loanAppId);

}
