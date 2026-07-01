package com.appsdeveloperblog.photoapp.api.users.security;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.core.env.Environment;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import com.appsdeveloperblog.photoapp.api.users.service.UsersService;
import com.appsdeveloperblog.photoapp.api.users.shared.UserDto;
import com.appsdeveloperblog.photoapp.api.users.ui.model.LoginRequestModel;
import com.fasterxml.jackson.databind.ObjectMapper;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class AuthenticationFilter extends UsernamePasswordAuthenticationFilter {

    private final AuthenticationManager authenticationManager;
    private final UsersService usersService;
    private final Environment environment;

    public AuthenticationFilter(AuthenticationManager authenticationManager,
                                UsersService usersService,
                                Environment environment) {

        this.authenticationManager = authenticationManager;
        this.usersService = usersService;
        this.environment = environment;

        // Optional: set the AuthenticationManager explicitly
        setAuthenticationManager(authenticationManager);
    }

    @Override
    public Authentication attemptAuthentication(HttpServletRequest request,
                                                HttpServletResponse response)
            throws AuthenticationException {

        try {

            LoginRequestModel credentials =
                    new ObjectMapper().readValue(request.getInputStream(),
                            LoginRequestModel.class);

            return authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentials.getEmail(),
                            credentials.getPassword(),
                            new ArrayList<>()));

        } catch (IOException ex) {
            throw new RuntimeException(ex);
        }
    }

    @Override
    protected void successfulAuthentication(HttpServletRequest request,
                                            HttpServletResponse response,
                                            FilterChain chain,
                                            Authentication authentication)
            throws IOException, ServletException {

        String username = ((User) authentication.getPrincipal()).getUsername();

        UserDto userDetails = usersService.getUserDetailsByEmail(username);

        String tokenSecret = environment.getProperty("token.secret");
        long expirationTime = Long.parseLong(
                environment.getProperty("token.expiration_time", "86400000"));

        SecretKey key = Keys.hmacShaKeyFor(
                tokenSecret.getBytes(StandardCharsets.UTF_8));

        String token = Jwts.builder()
                .subject(userDetails.getUserId())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expirationTime))
                .signWith(key)
                .compact();

        response.addHeader("token", token);
        response.addHeader("userId", userDetails.getUserId());
    }
}