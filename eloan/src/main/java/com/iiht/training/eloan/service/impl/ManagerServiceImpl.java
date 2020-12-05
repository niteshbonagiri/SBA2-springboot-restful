package com.iiht.training.eloan.service.impl;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.LoanDto;
import com.iiht.training.eloan.dto.LoanOutputDto;
import com.iiht.training.eloan.dto.ProcessingDto;
import com.iiht.training.eloan.dto.RejectDto;
import com.iiht.training.eloan.dto.SanctionDto;
import com.iiht.training.eloan.dto.SanctionOutputDto;
import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Loan;
import com.iiht.training.eloan.entity.ProcessingInfo;
import com.iiht.training.eloan.entity.SanctionInfo;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.exception.AlreadyFinalizedException;
import com.iiht.training.eloan.exception.AlreadyProcessedException;
import com.iiht.training.eloan.exception.ClerkNotFoundException;
import com.iiht.training.eloan.exception.LoanNotFoundException;
import com.iiht.training.eloan.exception.ManagerNotFoundException;
import com.iiht.training.eloan.repository.LoanRepository;
import com.iiht.training.eloan.repository.ProcessingInfoRepository;
import com.iiht.training.eloan.repository.SanctionInfoRepository;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.ManagerService;

@Service
public class ManagerServiceImpl implements ManagerService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Autowired
	private LoanRepository loanRepository;
	
	@Autowired
	private ProcessingInfoRepository pProcessingInfoRepository;
	
	@Autowired
	private SanctionInfoRepository sanctionInfoRepository;
	
	@Override
	public List<LoanOutputDto> allProcessedLoans() {
		List<Loan> processedLoans=this.loanRepository.findAllByStatus(1);
		List<LoanOutputDto> loanDtos = new ArrayList<LoanOutputDto>();
		for(Loan loan:processedLoans) {
			loanDtos.add(this.convertEntityToOutputDto(loan));
		}	
		return loanDtos;
	}

	@Override
	public RejectDto rejectLoan(Long managerId, Long loanAppId, RejectDto rejectDto) {
		
		Optional<Users> users=this.usersRepository.findById(managerId);
		if(users.isEmpty()) {
				throw new ManagerNotFoundException("Manager not found");
		}
		
		if(users.isPresent())
			{
				if(!users.get().getRole().equals("Manager")) {
					
					throw new ManagerNotFoundException("Manager not found");
			}
			
		}
		
		Loan loan=this.loanRepository.findById(loanAppId).orElse(null);
		if(loan==null) 
			throw new LoanNotFoundException("Loan not found");
		
		if(loan.getStatus().equals(2) || loan.getStatus().equals(-1))
			throw new AlreadyFinalizedException("Loan is already Processed");
		
		
		this.loanRepository.updateRemark(loanAppId, rejectDto.getRemark());
		this.loanRepository.updateStatus(loanAppId, -1);
		return rejectDto;
	}

	@Override
	public SanctionOutputDto sanctionLoan(Long managerId, Long loanAppId, SanctionDto sanctionDto) {
		
		Optional<Users> users=this.usersRepository.findById(managerId);
		if(users.isEmpty()) {
				throw new ManagerNotFoundException("Manager not found");
		}
		
		if(users.isPresent())
			{
				if(!users.get().getRole().equals("Manager")) {
					
					throw new ManagerNotFoundException("Manager not found");
			}
			
		}
		
		Loan loan=this.loanRepository.findById(loanAppId).orElse(null);
		if(loan==null) 
			throw new LoanNotFoundException("Loan not found");
		
		if(loan.getStatus().equals(2) || loan.getStatus().equals(-1))
			throw new AlreadyFinalizedException("Loan is already Processed");
		
		
		SanctionInfo entity=new SanctionInfo();
		Double emi=this.calculateEmi(sanctionDto.getLoanAmountSanctioned(), sanctionDto.getTermOfLoan());
		entity.setLoanAppId(loanAppId);
		entity.setManagerId(managerId);
		entity.setLoanAmountSanctioned(sanctionDto.getLoanAmountSanctioned());
		entity.setLoanClosureDate(this.calculateLoanClosureDate(sanctionDto.getPaymentStartDate(), sanctionDto.getLoanAmountSanctioned(), sanctionDto.getTermOfLoan(), emi));
		entity.setMonthlyPayment(emi);
		entity.setPaymentStartDate(sanctionDto.getPaymentStartDate());
		entity.setTermOfLoan(sanctionDto.getTermOfLoan());
		this.sanctionInfoRepository.save(entity);
		this.loanRepository.updateStatus(loanAppId, 2);
		
		return this.convertSanctionEntityToOutputDto(entity);
	}
	
	public Double calculateEmi(Double loanAmtSanctioned,Double term)  {
		
		Double termPaymentAmount=loanAmtSanctioned*(1+(10/100))+term;
		Double emi=termPaymentAmount/term;
		
		return emi;	
	}
	
	public String calculateLoanClosureDate(String paymentstrtdate,Double loanAmtSanctioned,Double term,Double emi)  {
		
		int months=(int) ((loanAmtSanctioned*(1+(10/100))+term)/emi);
		String loanclosureDate=LocalDate.parse(paymentstrtdate, DateTimeFormatter.ofPattern("yyyy-MM-dd")).plusMonths(months).toString();
		
		return loanclosureDate;	
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
	
	private SanctionOutputDto convertSanctionEntityToOutputDto(SanctionInfo sanction) {
		
		SanctionOutputDto output=new SanctionOutputDto();
		output.setLoanAmountSanctioned(sanction.getLoanAmountSanctioned());
		output.setLoanClosureDate(sanction.getLoanClosureDate());
		output.setMonthlyPayment(sanction.getMonthlyPayment());
		output.setPaymentStartDate(sanction.getPaymentStartDate());
		output.setTermOfLoan(sanction.getTermOfLoan());
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
