package com.example.demo;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

@AutoConfigureMockMvc
@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationApiIntegrationTest {

	@Autowired
	MockMvc mockMvc;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	BCryptPasswordEncoder encoder;
	@Test
	void 未認証では予約を作成できない() throws Exception{
		
		long before = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM reservations"
				, Long.class);
		
		mockMvc.perform(post("/reservations")
				       .contentType(MediaType.APPLICATION_JSON)
				       .content("""
				            {      
				              "stylistId": {"id" : 2},
				              "date": "2026-11-07T10:00:00",
				              "menuId": {"id": 3}
				             }
				             """))
		.andExpect(status().isForbidden());
		long after = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM reservations",
				Long.class);
		
		assertThat(after).isEqualTo(before);
	}
	
	@Test
	void 権限チェックでリクエストエラー() throws Exception {
		String hash_password = encoder.encode("password");
		jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (? ,? ,? ,? ,?)"""
				, 5, "hanako", "stylist@example.com", hash_password , "STYLIST");

		long rowCount = jdbcTemplate.queryForObject(
				"SELECT COUNT(*) FROM users WHERE id = 5"
				, Long.class);
		assertThat(rowCount).isEqualTo(1);
		
		String token = mockMvc.perform(post("/auth/login")
				                      .contentType(MediaType.APPLICATION_JSON)
				                      .content("""
											{
											  "email": "stylist@example.com",
											  "password": "password"
											}"""))
				                      .andExpect(status().isOk())
				                      .andReturn().getResponse().getContentAsString();
		assertThat(token).isNotBlank();
		
		long before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
		mockMvc.perform(
					post("/reservations")
					.header("Authorization", "Bearer " + token)
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
							{
							  "stylistId": {"id": 2},
							  "date": "2026-11-07T10:00:00",
							  "menuId": {"id": 3}
							}"""))
		.andExpect(status().isForbidden());
		long after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
		assertThat(after).isEqualTo(before);
		
	}
	@AfterEach
	void clear() {
		jdbcTemplate.update("DELETE FROM users WHERE id = 5");
	}

}
