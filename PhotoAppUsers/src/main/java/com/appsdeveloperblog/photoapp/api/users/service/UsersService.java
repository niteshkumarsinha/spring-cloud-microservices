package com.appsdeveloperblog.photoapp.api.users.service;

import org.springframework.security.core.userdetails.UserDetailsService;

import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;

public interface UsersService extends UserDetailsService {
	UserDto createUser(UserDto userDto);

	UserDto getUserDetailsByEmail(String username);

	UserDto getUserByUserId(String userId);

	
}
