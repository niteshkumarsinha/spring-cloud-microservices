package com.appsdeveloperblog.photoapp.api.users.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.expression.WebExpressionAuthorizationManager;

import com.appsdeveloperblog.photoapp.api.users.service.UsersService;

@Configuration
@EnableWebSecurity
public class WebSecurity {

	private final Environment env;
	private final UsersService usersService;
	private final BCryptPasswordEncoder passwordEncoder;

	public WebSecurity(Environment env, UsersService usersService, BCryptPasswordEncoder passwordEncoder) {
		this.env = env;
		this.usersService = usersService;
		this.passwordEncoder = passwordEncoder;
	}

	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {

		String gatewayIp = env.getProperty("gateway.ip");
		String loginUrl = env.getProperty("login.url.path");

		AuthenticationManagerBuilder authenticationManagerBuilder = http
				.getSharedObject(AuthenticationManagerBuilder.class);

		authenticationManagerBuilder.userDetailsService(usersService).passwordEncoder(passwordEncoder);

		AuthenticationManager authenticationManager = authenticationManagerBuilder.build();

		AuthenticationFilter authenticationFilter = new AuthenticationFilter(authenticationManager, usersService, env);

		// Prevent "Pattern cannot be null or empty"
		authenticationFilter.setFilterProcessesUrl(loginUrl != null ? loginUrl : "/users/login");

		http.csrf(csrf -> csrf.disable())

				.authorizeHttpRequests(auth -> auth

						.requestMatchers("/h2-console/**").permitAll()

						.requestMatchers("/actuator/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/users")
							.access(new WebExpressionAuthorizationManager("hasIpAddress('" + gatewayIp + "')"))

						.requestMatchers(HttpMethod.GET, "/users/status/check")
							.access(new WebExpressionAuthorizationManager("hasIpAddress('" + gatewayIp + "')"))
						.requestMatchers(HttpMethod.GET, "/users/**")
							.access(new WebExpressionAuthorizationManager("hasIpAddress('" + gatewayIp + "')"))

						.anyRequest().authenticated())

				.authenticationManager(authenticationManager)

				.addFilter(authenticationFilter)

				.sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

				.headers(headers -> headers.frameOptions(frame -> frame.sameOrigin()));

		return http.build();
	}
}