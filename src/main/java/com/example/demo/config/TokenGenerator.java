package com.example.demo.config;

import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import com.example.demo.domain.entity.User;

import io.jsonwebtoken.Jwts;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@AllArgsConstructor
@Slf4j
@Component
public class TokenGenerator {

	private SecretKey secretKey;
	
	public String generateToken(User user) {
		
		String token = null;	
		try {
			DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd");
			LocalDate now = LocalDate.now();
			Date  parsed_time= dateFormat.parse(now.toString());
			Date expiration = dateFormat.parse(now.plusDays(7).toString());
		    token = Jwts.builder()
		    		  .subject(user.getEmail()) // ユーザー識別子
		    		  .claim("role", user.getRole())
		    		  .claim("id", user.getId())
		    		  .issuedAt(parsed_time) // 発行日時
		    		  .expiration(expiration) // 有効期限
		    		  .signWith(secretKey) // 秘密鍵
		    		  .compact(); // JWTの生成
		    log.info("作成されたトークン={}", token);
			
		}catch(ParseException e) {
			log.info("トークンの作成に失敗しました:{}", e.getMessage());
		}
		return token;

	}
}
