package com.appsdeveloperblog.photoapp.api.gateway;


import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.cloud.gateway.filter.GatewayFilter;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;


@Component
public class MyPostFilter implements GlobalFilter, Ordered{

	final Logger logger = LoggerFactory.getLogger(MyPostFilter.class);
	
	@Override
	public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
		
		return chain.filter(exchange).then(Mono.fromRunnable(() -> {
			// Add some code
			logger.info("Last Post filter executed...");
			String requestPath = exchange.getRequest().getPath().toString();
			logger.info("Request Path = " + requestPath);
			HttpHeaders headers = exchange.getRequest().getHeaders();
			Set<String> headerNames = headers.keySet();
			headerNames.forEach((headerName) -> {
				String headerValue = headers.getFirst(headerName);
				logger.info(headerName + " " + headerValue);
			});
		}));
		
	}

	@Override
	public int getOrder() {
		return 0;
	}
	
	
}
