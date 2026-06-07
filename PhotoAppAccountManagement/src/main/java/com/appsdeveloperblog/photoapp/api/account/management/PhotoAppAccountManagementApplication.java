package com.appsdeveloperblog.photoapp.api.account.management;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@SpringBootApplication
@EnableDiscoveryClient
public class PhotoAppAccountManagementApplication {

	public static void main(String[] args) {
		SpringApplication.run(PhotoAppAccountManagementApplication.class, args);
	}

}
