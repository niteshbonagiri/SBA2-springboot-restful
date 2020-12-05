package com.iiht.training.eloan.service.impl;

import java.util.ArrayList;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.iiht.training.eloan.dto.UserDto;
import com.iiht.training.eloan.entity.Users;
import com.iiht.training.eloan.repository.UsersRepository;
import com.iiht.training.eloan.service.AdminService;

@Service
public class AdminServiceImpl implements AdminService {

	@Autowired
	private UsersRepository usersRepository;
	
	@Override
	public UserDto registerClerk(UserDto userDto) {
		Users user = new Users();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setMobile(userDto.getMobile());
		user.setRole("Clerk");
		this.usersRepository.save(user);
		userDto.setId(user.getId());
		return userDto;
	}

	@Override
	public UserDto registerManager(UserDto userDto) {
		Users user = new Users();
		user.setFirstName(userDto.getFirstName());
		user.setLastName(userDto.getLastName());
		user.setEmail(userDto.getEmail());
		user.setMobile(userDto.getMobile());
		user.setRole("Manager");
		this.usersRepository.save(user);
		userDto.setId(user.getId());
		return userDto;
	}

	@Override
	public List<UserDto> getAllClerks() {
		List<Users> users = this.usersRepository.findAll();

		List<UserDto> userDtos = new ArrayList<UserDto>();
		for(Users user : users) {
			if(user.getRole().equals("Clerk")) {
			UserDto dto = this.convertEntityToOutputDto(user);
			userDtos.add(dto);
			}
		}
		return userDtos;
	}

	private UserDto convertEntityToOutputDto(Users user) {
		UserDto dto=new UserDto();
		dto.setId(user.getId());
		dto.setFirstName(user.getFirstName());
		dto.setLastName(user.getLastName());
		dto.setEmail(user.getEmail());
		dto.setMobile(user.getMobile());
		return dto;
	}

	@Override
	public List<UserDto> getAllManagers() {
		List<Users> users = this.usersRepository.findAll();

		List<UserDto> userDtos = new ArrayList<UserDto>();
		for(Users user : users) {
			if(user.getRole().equals("Manager")) {
			UserDto dto = this.convertEntityToOutputDto(user);
			userDtos.add(dto);
			}
		}
		return userDtos;
	}

}
