package com.appsdeveloperblog.photoapp.api.gateway.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfiguration;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class WebSecurity{
	
	private final Environment env;

    private final AuthenticationManager authenticationManager;

    public WebSecurity(Environment env, AuthenticationManager authenticationManager) {
        this.env = env;
        this.authenticationManager = authenticationManager;
    }
	
	@Bean
	public SecurityFilterChain configure(HttpSecurity http) throws Exception {
		http.csrf().disable();
		http.headers().frameOptions().disable();
		http.sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS);
		http.authorizeRequests()
		.requestMatchers(env.getProperty("h2.console.url.path")).permitAll()
		.requestMatchers(HttpMethod.POST, env.getProperty("api.registration.url.path")).permitAll()
		.requestMatchers(HttpMethod.POST, env.getProperty("api.login.url.path")).permitAll()
		.anyRequest().authenticated()
		.and()
		.addFilter(new AuthorizationFilter(authenticationManager, env));
		return http.build();
	}
	
}
