package com.appsdeveloperblog.photoapp.api.users.service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.modelmapper.ModelMapper;
import org.modelmapper.convention.MatchingStrategies;
import org.modelmapper.spi.MatchingStrategy;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.appsdeveloperblog.photoapp.api.users.model.UserEntity;
import com.appsdeveloperblog.photoapp.api.users.repository.UsersRepository;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.AlbumResponseModel;

@Service
public class UserServiceImpl implements UsersService{

	private final UsersRepository usersRepository;
	private final BCryptPasswordEncoder bcryptPasswordEncoder;
	private final RestTemplate restTemplate;
	private final Environment env;
	
	@Autowired
	public UserServiceImpl(UsersRepository usersRepository, BCryptPasswordEncoder bcryptPasswordEncoder, RestTemplate restTemplate, Environment environment) {
		super();
		this.usersRepository = usersRepository;
		this.bcryptPasswordEncoder = bcryptPasswordEncoder;
		this.restTemplate = restTemplate;
		this.env = environment;
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


	@Override
	public UserDto getUserByUserId(String userId) {
		UserEntity userEntity = usersRepository.findByUserId(userId);
		if(userEntity == null) {
			throw new UsernameNotFoundException("user not found");
		}
		ModelMapper mapper = new ModelMapper();
		UserDto returnValue = mapper.map(userEntity, UserDto.class);
		String albumsURL = String.format(env.getProperty("albums.url"), userId);
		ResponseEntity<List<AlbumResponseModel>> albumsListResponse = restTemplate.exchange(albumsURL, HttpMethod.GET, null, new ParameterizedTypeReference<List<AlbumResponseModel>>(){});
		
		List<AlbumResponseModel> albumsList = albumsListResponse.getBody();
		
		returnValue.setAlbums(albumsList);
		
		return returnValue;
	}

}
