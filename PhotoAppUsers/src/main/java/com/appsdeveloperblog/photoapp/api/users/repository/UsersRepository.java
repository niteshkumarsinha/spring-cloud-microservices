package com.appsdeveloperblog.photoapp.api.users.repository;

import org.springframework.data.repository.CrudRepository;

import com.appsdeveloperblog.photoapp.api.users.model.UserEntity;

public interface UsersRepository extends CrudRepository<UserEntity, Long> {
	UserEntity findByEmail(String email);
}
