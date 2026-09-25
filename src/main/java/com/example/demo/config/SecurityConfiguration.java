package com.example.demo.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import lombok.AllArgsConstructor;

@Configuration
@AllArgsConstructor
public class SecurityConfiguration {

	JwtAuthenticationFilter jwtAuthenticationFilter;
	
	@Bean
	SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			.authorizeHttpRequests((requests) -> requests
				.requestMatchers("/admin/**", "/register/**", 
						"/auth/**", "/health/**", "/actuator/health/**").permitAll()
				.anyRequest().authenticated()
			)
			// JWT認証フィルターをUsernamePasswordAuthenticationFilterの前に追加
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class) 

			.csrf((csrf) -> csrf.disable())
			.logout((logout) -> logout.permitAll());
		return http.build();
	}
	
	@Bean
	BCryptPasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
