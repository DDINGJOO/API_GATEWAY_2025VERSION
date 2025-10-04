package com.study.api_gateway.client;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class CommentClient {
	
	private final WebClient webClient;
	
	
	public CommentClient(@Qualifier(value = "commentWebClient") WebClient webClient) {
		this.webClient = webClient;
	}
	
	
	
}
