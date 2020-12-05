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
import com.iiht.training.eloan.exception.ClerkNotFoundException;
import com.iiht.training.eloan.exception.CustomerNotFoundException;
import com.iiht.training.eloan.exception.InvalidDataException;
import com.iiht.training.eloan.exception.LoanNotFoundException;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.CustomerService;

@Service
public class CustomerServiceImpl implements CustomerService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository pProcessingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
	@Override
	public UserDto register(UserDto userDto) {
		Users user = new Users();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setMobile(userDto.getMobile());
		user.setRole("Customer");
		this.usersRepository.save(user);
		userDto.setId(user.getId());
		return userDto;
	}

	@Override
	public LoanOutputDto applyLoan(Long customerId, LoanDto loanDto) {
		Optional<Users> users=this.usersRepository.findById(customerId);
		if(users.isEmpty()) 
			throw new CustomerNotFoundException("Customer not found"); 
		
		if(users.isPresent())
		{
			if(!users.get().getRole().equals("Customer")) {
				throw new CustomerNotFoundException("Customer not found");
			}
		}
		
		Loan loan=new Loan();
		loan.setCustomerId(customerId);
		loan.setLoanName(loanDto.getLoanName());
		loan.setLoanAmount(loanDto.getLoanAmount());
		loan.setLoanApplicationDate(loanDto.getLoanApplicationDate());
		loan.setBillingIndicator(loanDto.getBillingIndicator());
		loan.setBusinessStructure(loanDto.getBusinessStructure());
		loan.setTaxIndicator(loanDto.getTaxIndicator());
		loan.setStatus(0);
		this.loanRepository.save(loan);
		LoanOutputDto output=new LoanOutputDto();
		output.setCustomerId(loan.getCustomerId());
		output.setLoanAppId(loan.getId());
		output.setLoanDto(loanDto);
		//find by id to be updated
		output.setProcessingDto(this.convertProcessingEntityToDto(this.pProcessingInfoRepository.findById(customerId).orElse(null)));
		output.setRemark("");
		output.setSanctionOutputDto(this.convertSanctionEntityToDto(this.sanctionInfoRepository.findById(customerId).orElse(null)));
		output.setStatus("Applied");
		output.setUserDto(this.convertUserEntityToDto(this.usersRepository.findById(customerId).orElse(null)));
		return output;
	}
	

	@Override
	public LoanOutputDto getStatus(Long loanAppId) {
		Loan loan=this.loanRepository.findById(loanAppId).orElse(null);
		if(loan==null) 
			throw new LoanNotFoundException("Loan not found");
		LoanOutputDto output=new LoanOutputDto();
		output.setCustomerId(loan.getCustomerId());
		output.setLoanAppId(loan.getId());
		output.setLoanDto(this.convertLoanEntityToDto(loan));
		output.setProcessingDto(this.convertProcessingEntityToDto(this.pProcessingInfoRepository.findById(loanAppId).orElse(null)));
		output.setRemark(loan.getRemark());
		output.setSanctionOutputDto(this.convertSanctionEntityToDto(this.sanctionInfoRepository.findByLoanAppId(loanAppId)));
		output.setStatus(loan.getStatus().toString());
		output.setUserDto(this.convertUserEntityToDto(this.usersRepository.findById(loan.getCustomerId()).orElse(null)));
		return output;
	}

	@Override
	public List<LoanOutputDto> getStatusAll(Long customerId) {
		
		Optional<Users> users=this.usersRepository.findById(customerId);
		if(users.isEmpty()) 
			throw new CustomerNotFoundException("Customer not found"); 
		
		if(users.isPresent())
		{
			if(!users.get().getRole().equals("Customer")) {
				throw new CustomerNotFoundException("Customer not found");
			}
		}
		
		
		
		List<Loan> myLoans=this.loanRepository.findAllByCustomerId(customerId);
		if(myLoans.isEmpty()) 
			throw new LoanNotFoundException("Loan not found");
		List<LoanOutputDto> loanDtos = new ArrayList<LoanOutputDto>();
		for(Loan loan:myLoans) {
			loanDtos.add(this.convertEntityToOutputDto(loan));
		}	
		return loanDtos;

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
