package com.appsdeveloperblog.photoapp.api.gateway.security;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.www.BasicAuthenticationFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;



public class AuthorizationFilter extends BasicAuthenticationFilter {
	
	
	Environment environment;
	

	public AuthorizationFilter(AuthenticationManager authenticationManager, Environment environment) {
		super(authenticationManager);
		this.environment = environment;
	}
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
			throws IOException, ServletException {
		
		String authorizationHeader = request.getHeader(environment.getProperty("authorization.token.header.name"));
		
		if(authorizationHeader == null || !authorizationHeader.startsWith(environment.getProperty("authorization.token.header.prefix"))) {
			chain.doFilter(request, response);
			return;
		}
		
		UsernamePasswordAuthenticationToken authentication = getAuthentication(request);
		SecurityContextHolder.getContext().setAuthentication(authentication);
		chain.doFilter(request, response);
	}

	private UsernamePasswordAuthenticationToken getAuthentication(HttpServletRequest request) {
		String authorizationHeader = request.getHeader(environment.getProperty("authorization.token.header.name"));
		if(authorizationHeader == null) {
			return null;
		}
		String token = authorizationHeader.replace(environment.getProperty("authorization.token.header.prefix"), "").trim();
		String secret = environment.getProperty("token.secret");

		SecretKey key = Keys.hmacShaKeyFor(
		        secret.getBytes(StandardCharsets.UTF_8));

		Claims claims = Jwts.parser()
		        .verifyWith(key)
		        .build()
		        .parseSignedClaims(token)
		        .getPayload();

		String userId = claims.getSubject();
		if(userId == null)
			return null;
		
		return new UsernamePasswordAuthenticationToken(userId, null, new ArrayList<>());
	}
	
	
}
