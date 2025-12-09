package com.study.api_gateway.util;

import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URI;

@Component
public class RequestPathHelper {
	
	public String extractClientPath(ServerHttpRequest request) {
		if (request == null) return null;
		
		// 1) Prefer reverse-proxy provided original URI
		String original = getFirstHeader(request, "X-Original-URI");
		if (StringUtils.hasText(original)) {
			return original;
		}
		
		// 2) Try X-Forwarded-Uri / X-Forwarded-Path possibly with X-Forwarded-Prefix
		String forwardedUri = getFirstHeader(request, "X-Forwarded-Uri");
		String forwardedPath = getFirstHeader(request, "X-Forwarded-Path");
		String forwardedPrefix = getFirstHeader(request, "X-Forwarded-Prefix");
		String candidate = StringUtils.hasText(forwardedUri) ? forwardedUri : forwardedPath;
		if (StringUtils.hasText(candidate)) {
			String prefixed = (StringUtils.hasText(forwardedPrefix) ? normalize(forwardedPrefix) : "") + normalize(candidate);
			return ensureLeadingSlash(prefixed);
		}
		
		// 3) Fallback: use current request path and strip internal gateway prefix like "/bff"
		URI uri = request.getURI();
		String path = uri.getRawPath();
		String query = uri.getRawQuery();
		if (path == null) path = "";
		
		// strip known internal prefix
		if (path.startsWith("/bff/")) {
			path = path.substring(4); // remove "/bff"
		} else if (path.equals("/bff")) {
			path = "/"; // root
		}
		
		if (StringUtils.hasText(query)) {
			return path + "?" + query;
		}
		return path;
	}
	
	public String extractClientUrl(ServerHttpRequest request) {
		if (request == null) return null;
		String pathWithQuery = extractClientPath(request);
		
		// Determine scheme
		String scheme = firstNonEmpty(
				getFirstHeader(request, "X-Forwarded-Proto"),
				request.getURI() != null ? request.getURI().getScheme() : null,
				"http"
		);
		
		// Determine host (and optional port)
		String host = firstNonEmpty(
				getFirstHeader(request, "X-Forwarded-Host"),
				// Standard Host header
				getFirstHeader(request, "Host"),
				request.getHeaders().getHost() != null ? request.getHeaders().getHost().toString() : null
		);
		
		if (!StringUtils.hasText(host)) {
			// last resort: use server name from URI
			host = request.getURI() != null && request.getURI().getHost() != null
					? request.getURI().getHost()
					: "localhost";
			int port = request.getURI() != null ? request.getURI().getPort() : -1;
			if (port > 0) host = host + ":" + port;
		}
		
		// Ensure path begins with /
		String p = pathWithQuery;
		if (p == null || p.isBlank()) p = "/";
		if (!p.startsWith("/")) p = "/" + p;
		
		return scheme + "://" + host + p;
	}
	
	private String firstNonEmpty(String... values) {
		if (values == null) return null;
		for (String v : values) {
			if (StringUtils.hasText(v)) return v;
		}
		return null;
	}
	
	private String getFirstHeader(ServerHttpRequest request, String name) {
		var vals = request.getHeaders().get(name);
		return (vals != null && !vals.isEmpty()) ? vals.get(0) : null;
	}
	
	private String normalize(String p) {
		if (p == null) return "";
		// ensure it starts with /
		if (!p.startsWith("/")) p = "/" + p;
		// remove trailing / for prefix to avoid double slashes when concatenating
		if (p.endsWith("/")) p = p.substring(0, p.length() - 1);
		return p;
	}
	
	private String ensureLeadingSlash(String p) {
		if (p == null || p.isEmpty()) return "/";
		return p.startsWith("/") ? p : "/" + p;
	}
}
