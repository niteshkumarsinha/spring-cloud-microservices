package com.appsdeveloperblog.photoapp.api.gateway.security;

import java.nio.charset.StandardCharsets;

import javax.crypto.SecretKey;

import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.factory.AbstractGatewayFilterFactory;
import org.springframework.core.env.Environment;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;

import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import reactor.core.publisher.Mono;

@Component
public class AuthorizationHeader extends AbstractGatewayFilterFactory<AuthorizationHeader.Config> {

    private final Environment env;

    public AuthorizationHeader(Environment env) {
        super(Config.class);
        this.env = env;
    }

    public static class Config {
    }

    @Override
    public GatewayFilter apply(Config config) {

        return (exchange, chain) -> {

            ServerHttpRequest request = exchange.getRequest();

            // Check if Authorization header exists
            if (!request.getHeaders().containsKey(HttpHeaders.AUTHORIZATION)) {
                return onError(exchange, "Missing Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String authorizationHeader = request.getHeaders().getFirst(HttpHeaders.AUTHORIZATION);

            if (authorizationHeader == null || !authorizationHeader.startsWith("Bearer ")) {
                return onError(exchange, "Invalid Authorization Header", HttpStatus.UNAUTHORIZED);
            }

            String jwt = authorizationHeader.substring(7);

            if (!isJwtValid(jwt)) {
                return onError(exchange, "JWT token is not valid", HttpStatus.UNAUTHORIZED);
            }

            return chain.filter(exchange);
        };
    }

    private Mono<Void> onError(ServerWebExchange exchange,
                               String errorMessage,
                               HttpStatus status) {

        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(status);

        // Optional: send error message in response body
        // DataBuffer buffer = response.bufferFactory()
        //         .wrap(errorMessage.getBytes(StandardCharsets.UTF_8));
        // return response.writeWith(Mono.just(buffer));

        return response.setComplete();
    }

    private boolean isJwtValid(String jwt) {

        String secret = env.getProperty("token.secret");

        if (secret == null || secret.isBlank()) {
            return false;
        }

        try {

            SecretKey key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));

            JwtParser parser = Jwts.parser()
                                   .verifyWith(key)
                                   .build();

            String subject = parser
                    .parseSignedClaims(jwt)
                    .getPayload()
                    .getSubject();

            return subject != null && !subject.isBlank();

        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }
}