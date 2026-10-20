package com.example.demo;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;

import com.example.demo.domain.Role;
import com.example.demo.domain.Status;

@SpringBootTest
@Import({TestcontainersConfiguration.class})
//		TestClockConfiguration.class})
@AutoConfigureMockMvc
public class ReviewServiceIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	MockMvc mockMvc;
	@Autowired
	BCryptPasswordEncoder encoder;
	@Nested
	@DisplayName("正常系")
	class 正常系 {
		@Test
		void 担当スタイリストが施術後にstatusを変更する() throws Exception{
			//arrange
			// ①テスト用のテーブル登録とトークンの取得
			String passwordHash = encoder.encode("password");
			jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""", 7, "mystylist", "stylist@example.com", passwordHash, Role.STYLIST.name());
			// stylist登録
			jdbcTemplate.update("""
				INSERT INTO stylists (user_id, bio)
				VALUES (?, ?)
				""", 7, "カットが得意です");
			String token = mockMvc.perform(post("/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content("""
				        		{"email": "stylist@example.com",
				        		 "password":  "password" 
				        		 }
				        		"""))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			assertThat(token).isNotBlank();
			//②テスト用のReservationの登録
			LocalDateTime startTime = LocalDateTime.of(2026, 10, 7, 15, 0, 0);
			LocalDateTime endTime = startTime.plusMinutes(90);
			Status status = Status.CONFIRMED;
			jdbcTemplate.batchUpdate("""
				INSERT INTO reservations (id, customer_id, stylist_id, menu_id,
				                          start_time, end_time, status)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""",List.of(
						new Object[] {1, 2, 7, 1, startTime, endTime, status.name()},
						new Object[] {2, 3, 7, 2, startTime, endTime, status.name()}
						));
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", long.class)).isEqualTo(2);
			// Act & Assert
			mockMvc.perform(patch("/reservations/{id}/complete", 1)
					.header("Authorization", "Bearer " + token))
			.andExpect(status().isOk());
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", long.class)).isEqualTo(2);
			String actual = jdbcTemplate.queryForObject("SELECT status FROM reservations WHERE id = 1", String.class);
			assertThat(actual).isEqualTo(Status.COMPLETED.name());
			
			Map<String, Object> result = jdbcTemplate.queryForMap("SELECT * FROM reservations WHERE id = 1");
			assertThat((long)result.get("id")).isEqualTo(1);
			assertThat((long)result.get("stylist_id")).isEqualTo(7);
			assertThat((long)result.get("menu_id")).isEqualTo(1);
			assertThat((long)result.get("customer_id")).isEqualTo(2);
			assertThat((LocalDateTime)result.get("start_time")).isEqualTo(startTime);
			assertThat((LocalDateTime)result.get("end_time")).isEqualTo(endTime);
			assertThat((String)result.get("status")).isEqualTo(Status.COMPLETED.name());
		
			result = jdbcTemplate.queryForMap("SELECT * FROM reservations WHERE id = 2");
			assertThat((long)result.get("id")).isEqualTo(2);
			assertThat((long)result.get("stylist_id")).isEqualTo(7);
			assertThat((long)result.get("menu_id")).isEqualTo(2);
			assertThat((long)result.get("customer_id")).isEqualTo(3);
			assertThat((LocalDateTime)result.get("start_time")).isEqualTo(startTime);
			assertThat((LocalDateTime)result.get("end_time")).isEqualTo(endTime);
			assertThat((String)result.get("status")).isEqualTo(Status.CONFIRMED.name());
		}	
	}
	
	@Nested
	@DisplayName("異常系")
	class 異常系 {
		
		@Test
		void 未認証の場合は403エラーが発生する () throws Exception{
			long reservationId = 1L;
			mockMvc.perform(patch("/reservations/{id}/complete", reservationId))
			.andExpect(status().isForbidden());
		}
		
		@Test
		void 認証済みの別担当者の場合403エラーが発生する() throws Exception {
			
			// Arrange
			  // パラメータ準備
			String passwordHash = encoder.encode("password");
			  // スタイリスト登録
			long stylistId = 7L;
			jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""", stylistId, "ann", "stylist@example.com", passwordHash, Role.STYLIST.name());
			jdbcTemplate.update("""
					INSERT INTO stylists (user_id, bio)
					VALUES (?, ?)
					""", 7, "カットが得意です");
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = " + stylistId, Long.class)).isEqualTo(1);
			  // 予約登録
			long reservationId = 1L;
			long otherStylistId = 3L;
			long customerId = 2L;
			long menuId = 3L;
			LocalDateTime startTime = LocalDateTime.of(2026, 10, 7, 15, 0, 0);
			LocalDateTime endTime = startTime.plusMinutes(90);
			Status status = Status.CONFIRMED;
			jdbcTemplate.update("""
					INSERT INTO reservations (id, customer_id, stylist_id, menu_id,
					                          start_time, end_time, status)
					VALUES (?, ?, ?, ?, ?, ?, ?)
					""",reservationId, customerId, otherStylistId, menuId, startTime, endTime, status.name());
			long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
			assertThat(rowCount).isEqualTo(1);
			
			String json = """
					{
					"email": "stylist@example.com",
					"password": "password"
					} """;
			String token = mockMvc.perform(post("/auth/login")
					       .contentType(MediaType.APPLICATION_JSON)
					       .content(json))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		    assertThat(token).isNotBlank();
		    
		    mockMvc.perform(patch("/reservations/{id}/complete", reservationId)
		    		       .header("Authorization", "Bearer " + token))
		    .andExpect(status().isForbidden());
		   rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
		   Map<String, Object> actual = jdbcTemplate.queryForMap("SELECT * FROM reservations");
		   assertThat(rowCount).isEqualTo(1);
		   assertThat(actual.get("status")).isEqualTo(Status.CONFIRMED.name());
		}
		
		@Test
		void 存在しない予約を指定した場合404エラーが発生する() throws Exception {
			// Arrange
			  // パラメータ準備
			String passwordHash = encoder.encode("password");
			  // スタイリスト登録
			long stylistId = 7L;
			jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""", stylistId, "ann", "stylist@example.com", passwordHash, Role.STYLIST.name());
			jdbcTemplate.update("""
					INSERT INTO stylists (user_id, bio)
					VALUES (?, ?)
					""", 7, "カットが得意です");
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = " + stylistId, Long.class)).isEqualTo(1);
		    
			long notExistReservationId = 999L;
			String json = """
					{
					"email": "stylist@example.com",
					"password": "password"
					} """;
			String token = mockMvc.perform(post("/auth/login")
					       .contentType(MediaType.APPLICATION_JSON)
					       .content(json))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		    assertThat(token).isNotBlank();
		    
		    mockMvc.perform(patch("/reservations/{id}/complete", notExistReservationId)
		    		       .header("Authorization", "Bearer " + token))
		    .andExpect(status().isNotFound());
		}
		@Test
		void 予約の終了時間前にステータスを更新した場合409エラーが発生する () throws Exception {
			// Arrange
			  // パラメータ準備
			String passwordHash = encoder.encode("password");
			  // スタイリスト登録
			long stylistId = 7L;
			jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""", stylistId, "ann", "stylist@example.com", passwordHash, Role.STYLIST.name());
			jdbcTemplate.update("""
					INSERT INTO stylists (user_id, bio)
					VALUES (?, ?)
					""", 7, "カットが得意です");
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = " + stylistId, Long.class)).isEqualTo(1);
		    
			long reservationId = 1L;
			long customerId = 2L;
			long menuId = 3L;
			LocalDateTime startTime = LocalDateTime.of(2050, 12, 31, 15, 00, 00);
			LocalDateTime endTime = startTime.plusMinutes(90);
			Status status = Status.CONFIRMED;
			jdbcTemplate.update("""
					INSERT INTO reservations (id, customer_id, stylist_id, menu_id,
					                          start_time, end_time, status)
					VALUES (?, ?, ?, ?, ?, ?, ?)
					""",reservationId, customerId, stylistId, menuId, startTime, endTime, status.name());
			long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
			assertThat(rowCount).isEqualTo(1);
		
			String json = """
					{
					"email": "stylist@example.com",
					"password": "password"
					} """;
			String token = mockMvc.perform(post("/auth/login")
					       .contentType(MediaType.APPLICATION_JSON)
					       .content(json))
			.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
		    assertThat(token).isNotBlank();
		    
			// Act & Assert
		    mockMvc.perform(patch("/reservations/{id}/complete", reservationId)
		    		       .header("Authorization", "Bearer " + token))
		    .andExpect(status().isConflict());
		    rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
		   Map<String, Object> actual = jdbcTemplate.queryForMap("SELECT * FROM reservations");
		   assertThat(rowCount).isEqualTo(1);
		   assertThat(actual.get("status")).isEqualTo(Status.CONFIRMED.name());
		
		}
		@Test
		void COMPLETEDステータスを更新した場合409エラーが発生する () throws Exception {
			// Arrange
			// パラメータ準備
			String passwordHash = encoder.encode("password");
			// スタイリスト登録
			long stylistId = 7L;
			jdbcTemplate.update("""
				INSERT INTO users (id, name, email, password_hash, role)
				VALUES (?, ?, ?, ?, ?)
				""", stylistId, "ann", "stylist@example.com", passwordHash, Role.STYLIST.name());
			jdbcTemplate.update("""
					INSERT INTO stylists (user_id, bio)
					VALUES (?, ?)
					""", 7, "カットが得意です");
			assertThat(jdbcTemplate.queryForObject("SELECT COUNT(*) FROM users WHERE id = " + stylistId, Long.class)).isEqualTo(1);
			
			long reservationId = 1L;
			long customerId = 2L;
			long menuId = 3L;
			LocalDateTime startTime = LocalDateTime.of(2026, 10, 7, 15, 0);
			LocalDateTime endTime = startTime.plusMinutes(90);
			Status status = Status.COMPLETED;
			jdbcTemplate.update("""
					INSERT INTO reservations (id, customer_id, stylist_id, menu_id,
					                          start_time, end_time, status)
					VALUES (?, ?, ?, ?, ?, ?, ?)
					""",reservationId, customerId, stylistId, menuId, startTime, endTime, status.name());
			long rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
			assertThat(rowCount).isEqualTo(1);
			
			String json = """
					{
					"email": "stylist@example.com",
					"password": "password"
					} """;
			String token = mockMvc.perform(post("/auth/login")
					.contentType(MediaType.APPLICATION_JSON)
					.content(json))
					.andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
			assertThat(token).isNotBlank();
			
			// Act & Assert
			mockMvc.perform(patch("/reservations/{id}/complete", reservationId)
					.header("Authorization", "Bearer " + token))
			.andExpect(status().isConflict());
			rowCount = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Long.class);
			Map<String, Object> actual = jdbcTemplate.queryForMap("SELECT * FROM reservations");
			assertThat(rowCount).isEqualTo(1);
			assertThat(actual.get("status")).isEqualTo(Status.COMPLETED.name());
			
		}
	}
	@AfterEach
	void tearDown() {
		jdbcTemplate.update("DELETE FROM reservations WHERE id = 1");
		jdbcTemplate.update("DELETE FROM reservations WHERE id = 2");
		jdbcTemplate.update("DELETE FROM stylists WHERE user_id = 7");
		jdbcTemplate.update("DELETE FROM users WHERE id = 7");
		
	}
}
