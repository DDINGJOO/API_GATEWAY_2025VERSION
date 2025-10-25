package com.study.api_gateway.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class JwtTokenValidator {
	
	@Value("${security.jwt.secret}")
	private String jwtSecret;
	
	private static String base64UrlEncode(byte[] bytes) {
		return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
	}
	
	private static byte[] base64UrlDecode(String str) {
		return Base64.getUrlDecoder().decode(str);
	}
	
	// Minimal JSON parser for flat objects with string/number/boolean/null values
	private static Map<String, Object> parseJsonObject(String json) {
		Map<String, Object> map = new HashMap<>();
		if (json == null) return map;
		String s = json.trim();
		if (s.length() < 2 || s.charAt(0) != '{' || s.charAt(s.length() - 1) != '}') return map;
		s = s.substring(1, s.length() - 1).trim();
		if (s.isEmpty()) return map;
		int i = 0;
		while (i < s.length()) {
			// parse key
			if (s.charAt(i) != '"') break;
			int keyStart = ++i;
			StringBuilder keySb = new StringBuilder();
			boolean escaped = false;
			for (; i < s.length(); i++) {
				char c = s.charAt(i);
				if (escaped) {
					keySb.append(c);
					escaped = false;
				} else if (c == '\\') {
					escaped = true;
				} else if (c == '"') {
					i++;
					break;
				} else {
					keySb.append(c);
				}
			}
			// skip colon
			while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
			if (i >= s.length() || s.charAt(i) != ':') break;
			i++;
			while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
			// parse value
			Object value;
			if (i < s.length() && s.charAt(i) == '"') {
				// string
				i++;
				StringBuilder valSb = new StringBuilder();
				boolean esc = false;
				for (; i < s.length(); i++) {
					char c = s.charAt(i);
					if (esc) {
						valSb.append(c);
						esc = false;
					} else if (c == '\\') {
						esc = true;
					} else if (c == '"') {
						i++;
						break;
					} else {
						valSb.append(c);
					}
				}
				value = valSb.toString();
			} else {
				// literal (number, boolean, null)
				int start = i;
				while (i < s.length() && ",}".indexOf(s.charAt(i)) == -1) i++;
				String literal = s.substring(start, i).trim();
				if (literal.equals("null")) {
					value = null;
				} else if (literal.equals("true") || literal.equals("false")) {
					value = Boolean.valueOf(literal);
				} else {
					try {
						if (literal.contains(".") || literal.contains("e") || literal.contains("E")) {
							value = Double.valueOf(literal);
						} else {
							value = Long.valueOf(literal);
						}
					} catch (NumberFormatException ex) {
						value = literal;
					}
				}
			}
			map.put(keySb.toString(), value);
			// skip spaces and comma
			while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
			if (i < s.length() && s.charAt(i) == ',') {
				i++;
				while (i < s.length() && Character.isWhitespace(s.charAt(i))) i++;
			}
		}
		return map;
	}
	
	public boolean isValid(String token) {
		return validate(token).isValid();
	}
	
	public TokenValidationResult validate(String token) {
		try {
			// 토큰 형식 검증
			String[] parts;
			try {
				parts = splitToken(token);
			} catch (IllegalArgumentException e) {
				log.debug("Invalid token format: {}", e.getMessage());
				return TokenValidationResult.INVALID_FORMAT;
			}
			
			// 서명 검증
			String unsigned = parts[0] + "." + parts[1];
			String expectedSig = sign(unsigned, jwtSecret);
			if (!constantTimeEquals(expectedSig, parts[2])) {
				log.debug("Token signature validation failed");
				return TokenValidationResult.INVALID_SIGNATURE;
			}
			
			// Payload 파싱
			Map<String, Object> payload;
			try {
				payload = parsePayload(parts[1]);
			} catch (Exception e) {
				log.debug("Failed to parse token payload: {}", e.getMessage());
				return TokenValidationResult.MALFORMED;
			}
			
			// 필수 클레임 검증 (sub, exp)
			Object subObj = payload.get("sub");
			Object expObj = payload.get("exp");
			
			if (subObj == null || expObj == null) {
				log.debug("Token missing required claims. sub: {}, exp: {}", subObj, expObj);
				return TokenValidationResult.MISSING_CLAIMS;
			}
			
			// 만료 시간 검증
			if (expObj instanceof Number) {
				long exp = ((Number) expObj).longValue();
				long now = Instant.now().getEpochSecond();
				if (now >= exp) {
					log.debug("Token has expired. Current: {}, Expiration: {}", now, exp);
					return TokenValidationResult.EXPIRED;
				}
				return TokenValidationResult.VALID;
			}
			
			log.debug("Token expiration claim is not a number");
			return TokenValidationResult.MALFORMED;
		} catch (Exception e) {
			log.error("Unexpected token validation error: {}", e.getMessage(), e);
			return TokenValidationResult.MALFORMED;
		}
	}
	
	public String extractUserId(String token) {
		Map<String, Object> payload = safePayload(token);
		Object sub = payload.get("sub");
		return sub != null ? sub.toString() : null;
	}
	
	public String extractRole(String token) {
		Map<String, Object> payload = safePayload(token);
		Object role = payload.get("role");
		return role != null ? role.toString() : null;
	}
	
	public String extractDeviceId(String token) {
		Map<String, Object> payload = safePayload(token);
		Object deviceId = payload.get("deviceId");
		return deviceId != null ? deviceId.toString() : null;
	}
	
	public long extractExpiration(String token) {
		Map<String, Object> payload = safePayload(token);
		Object expObj = payload.get("exp");
		if (expObj instanceof Number) {
			long exp = ((Number) expObj).longValue();
			long now = Instant.now().getEpochSecond();
			return (exp - now);
		}
		return 0;
	}
	
	private Map<String, Object> safePayload(String token) {
		try {
			String[] parts = splitToken(token);
			return parsePayload(parts[1]);
		} catch (Exception e) {
			log.error("Error parsing token payload: {}", e.getMessage());
			return new HashMap<>();
		}
	}
	
	private String[] splitToken(String token) {
		if (token == null) throw new IllegalArgumentException("token is null");
		String[] parts = token.split("\\.");
		if (parts.length != 3) throw new IllegalArgumentException("invalid JWT format");
		return parts;
	}
	
	private Map<String, Object> parsePayload(String payloadB64) {
		byte[] jsonBytes = base64UrlDecode(payloadB64);
		String json = new String(jsonBytes, StandardCharsets.UTF_8);
		return parseJsonObject(json);
	}
	
	private boolean constantTimeEquals(String a, String b) {
		if (a == null || b == null) return false;
		if (a.length() != b.length()) return false;
		int result = 0;
		for (int i = 0; i < a.length(); i++) {
			result |= a.charAt(i) ^ b.charAt(i);
		}
		return result == 0;
	}
	
	private String sign(String data, String secret) {
		try {
			Mac mac = Mac.getInstance("HmacSHA256");
			SecretKeySpec keySpec =
					new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256");
			mac.init(keySpec);
			byte[] sig = mac.doFinal(data.getBytes(StandardCharsets.UTF_8));
			return base64UrlEncode(sig);
		} catch (Exception e) {
			log.error("Error signing token: {}", e.getMessage());
			return "";
		}
	}
}
