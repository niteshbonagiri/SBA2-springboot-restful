package com.iiht.training.eloan.service.impl;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.LoanDto;
import com.iiht.training.eloan.dto.LoanOutputDto;
import com.iiht.training.eloan.dto.ProcessingDto;
import com.iiht.training.eloan.dto.SanctionOutputDto;
import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Loan;
import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.exception.AlreadyProcessedException;
import com.iiht.training.eloan.exception.ClerkNotFoundException;
import com.iiht.training.eloan.exception.CustomerNotFoundException;
import com.iiht.training.eloan.exception.LoanNotFoundException;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.ClerkService;

@Service
public class ClerkServiceImpl implements ClerkService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository pProcessingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
	@Override
	public List<LoanOutputDto> allAppliedLoans() {
		List<Loan> appliedLoans=this.loanRepository.findAllByStatus(0);
		List<LoanOutputDto> loanDtos = new ArrayList<LoanOutputDto>();
		for(Loan loan:appliedLoans) {
			loanDtos.add(this.convertEntityToOutputDto(loan));
			
		}	
		return loanDtos;
	}

	@Override
	public ProcessingDto processLoan(Long clerkId, Long loanAppId, ProcessingDto processingDto) {
		Optional<Users> users=this.usersRepository.findById(clerkId);
		if(users.isEmpty()) {
				throw new ClerkNotFoundException("Clerk not found");
		}
		
		if(users.isPresent())
			{
				if(!users.get().getRole().equals("Clerk")) {
					
					throw new ClerkNotFoundException("Clerk not found");
			}
			
		}
		
		Loan loan=this.loanRepository.findById(loanAppId).orElse(null);
		if(loan==null) 
			throw new LoanNotFoundException("Loan not found");
		
		if(loan.getStatus().equals(1))
			throw new AlreadyProcessedException("Loan is already Processed");
		
		
		ProcessingInfo entity=new ProcessingInfo();
		entity.setLoanAppId(loanAppId);
		entity.setLoanClerkId(clerkId);
		entity.setAcresOfLand(processingDto.getAcresOfLand());
		entity.setAddressOfProperty(processingDto.getAddressOfProperty());
		entity.setAppraisedBy(processingDto.getAppraisedBy());
		entity.setLandValue(processingDto.getLandValue());
		entity.setSuggestedAmountOfLoan(processingDto.getSuggestedAmountOfLoan());
		entity.setValuationDate(processingDto.getValuationDate());
		this.pProcessingInfoRepository.save(entity);
		this.loanRepository.updateStatus(loanAppId, 1);
		return processingDto;
	}
	
	private LoanOutputDto convertEntityToOutputDto(Loan loan) {
	
		LoanOutputDto output=new LoanOutputDto();
		output.setCustomerId(loan.getCustomerId());
		output.setLoanAppId(loan.getId());
		output.setLoanDto(this.convertLoanEntityToDto(loan));
		//find by id to be updated
		output.setProcessingDto(this.convertProcessingEntityToDto(this.pProcessingInfoRepository.findByLoanAppId(loan.getId())));
		output.setRemark(loan.getRemark());
		output.setSanctionOutputDto(this.convertSanctionEntityToDto(this.sanctionInfoRepository.findByLoanAppId(loan.getId())));
		output.setStatus(loan.getStatus().toString());
		output.setUserDto(this.convertUserEntityToDto(this.usersRepository.findById(loan.getId()).orElse(null)));
		return output;
	
	}
	
	private LoanDto convertLoanEntityToDto(Loan loan) {
		LoanDto dto=new LoanDto();
		if(loan!=null) {
		dto.setBillingIndicator(loan.getBillingIndicator());
		dto.setBusinessStructure(loan.getBusinessStructure());
		dto.setLoanAmount(loan.getLoanAmount());
		dto.setLoanApplicationDate(loan.getLoanApplicationDate());
		dto.setLoanName(loan.getLoanName());
		dto.setTaxIndicator(loan.getTaxIndicator());
		}
		return dto;
		
	}
	
	private ProcessingDto convertProcessingEntityToDto(ProcessingInfo pinfo) {
		ProcessingDto d=new ProcessingDto();
		if(pinfo!=null) {
		d.setAcresOfLand(pinfo.getAcresOfLand());
		d.setAddressOfProperty(pinfo.getAddressOfProperty());
		d.setAppraisedBy(pinfo.getAppraisedBy());
		d.setLandValue(pinfo.getLandValue());
		d.setSuggestedAmountOfLoan(pinfo.getSuggestedAmountOfLoan());
		d.setValuationDate(pinfo.getValuationDate());
		}
		return d;
		
	}
	
	private SanctionOutputDto convertSanctionEntityToDto(SanctionInfo saninfo) {
		SanctionOutputDto s=new SanctionOutputDto();
		if(saninfo!=null) {
		s.setLoanAmountSanctioned(saninfo.getLoanAmountSanctioned());
		s.setLoanClosureDate(saninfo.getLoanClosureDate());
		s.setMonthlyPayment(saninfo.getMonthlyPayment());
		s.setPaymentStartDate(saninfo.getPaymentStartDate());
		s.setTermOfLoan(saninfo.getTermOfLoan());
		}
		return s;
		
	}
	
	private UserDto convertUserEntityToDto(Users users) {
		UserDto d=new UserDto();
		if(users!=null) {
		d.setEmail(users.getEmail());
		d.setFirstName(users.getFirstName());
		d.setLastName(users.getLastName());
		d.setMobile(users.getMobile());
		d.setId(users.getId());
		}
		return d;
		
	}

}
