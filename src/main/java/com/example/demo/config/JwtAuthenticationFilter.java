package com.example.demo.config;


import java.io.IOException;
import java.util.List;
import java.util.Objects;

import javax.crypto.SecretKey;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.JwtParser;
import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter{

	SecretKey secretKey;
	TokenGenerator tokenGenerator;
	
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
			throws ServletException, IOException {
		
		String authHeader = request.getHeader("Authorization");
		JwtParser parser = Jwts.parser().verifyWith(secretKey).build();
		
		try {
			if(!Objects.nonNull(authHeader)) {
				throw new JwtException("AuthorizationHeaderが空です");
			}
			Jws<Claims> claims = parser.parseSignedClaims(authHeader != null ? authHeader.substring(7) : null); // JWTの検証を行い、期限切れ等の場合は例外を投げる
			String email = claims.getPayload().getSubject();
			String role = claims.getPayload().get("role", String.class);

			SecurityContextHolder.getContext().setAuthentication(
					UsernamePasswordAuthenticationToken.authenticated(email, null, List.of(new SimpleGrantedAuthority("ROLE_" + role))));
			log.info("トークンの検証に成功しました:[email={},role={}]", email, role);
		}catch(JwtException e) {
			log.info("トークンの検証に失敗しました");
		}
		filterChain.doFilter(request, response);
	}

	
}
