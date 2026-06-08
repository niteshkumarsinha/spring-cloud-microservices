package com.appsdeveloperblog.photoapp.api.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

@Configuration
@EnableWebSecurity
public class WebSecurity {

	private final Environment env;

	public WebSecurity(Environment env) {
		this.env = env;
	}

	@Bean

	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		String gatewayIp = env.getProperty("gateway.ip");
		http
			.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authz -> authz
					.requestMatchers("/users").access(new WebExpressionAuthorizationManager("hasIpAddress('" + gatewayIp + "')"))
					.requestMatchers("/h2-console/**").permitAll()
					.anyRequest().authenticated()
			)
			.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()));

		return http.build();
	}
}
