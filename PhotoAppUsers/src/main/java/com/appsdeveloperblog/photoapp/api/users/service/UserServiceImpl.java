package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import com.appsdeveloperblog.photoapp.api.users.model.UserEntity;
import com.appsdeveloperblog.photoapp.api.users.repository.UsersRepository;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;

@Service
public class UserServiceImpl implements UsersService{

	private final UsersRepository usersRepository;
	private final BCryptPasswordEncoder bcryptPasswordEncoder;
	
	@Autowired
	public UserServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bcryptPasswordEncoder) {
		super();
		this.usersRepository = usersRepository;
		this.bcryptPasswordEncoder = bcryptPasswordEncoder;
	}


	@Override
	public UserDto createUser(UserDto userDto) {
		userDto.setUserId(UUID.randomUUID().toString());
		userDto.setEncryptedPassword(bcryptPasswordEncoder.encode(userDto.getPassword()));
		ModelMapper mapper = new ModelMapper();
		mapper.getConfiguration().setMatchingStrategy(MatchingStrategies.STRICT);
		UserEntity userEntity = mapper.map(userDto, UserEntity.class);
		UserEntity savedUser = usersRepository.save(userEntity);
		UserDto returnValue = mapper.map(savedUser, UserDto.class);
		return returnValue;
	}


	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		UserEntity userEntity = usersRepository.findByEmail(username);
		if(userEntity == null) {
			throw new UsernameNotFoundException(username);
			
		}
		return new User(userEntity.getEmail(), userEntity.getEncryptedPassword(), true, true, true, true, new ArrayList<>());
	}


	@Override
	public UserDto getUserDetailsByEmail(String username) {
		UserEntity userEntity = usersRepository.findByEmail(username);
		if(userEntity == null) {
			throw new UsernameNotFoundException(username);
		}
		return new ModelMapper().map(userEntity, UserDto.class);
	}

}
