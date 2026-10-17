package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;
import org.springframework.context.event.EventListener;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@SpringBootTest
@Import({
	TestcontainersConfiguration.class,
	StylistServiceIntegrationTest.FailureConfig.class
})
class StylistServiceIntegrationTest {

	@Autowired
	private StylistService stylistService;
	@Autowired
	private JdbcTemplate jdbcTemplate;
	
	@Test
	void イベントリスナーで例外が発生してロールバックする() {
		
		//  データベースの仕込み
		long id = 100L;
		long customerId = 1L;
		long stylistId = 2L;
		long menuId = 3L;
		LocalDateTime startTime = LocalDateTime.of(2026, 10, 1, 12, 0, 0);
		LocalDateTime endTime = LocalDateTime.of(2026, 10, 1, 13, 0, 0);
		Status status = Status.PENDING;
		
		jdbcTemplate.update("""
				INSERT INTO reservations (id, customer_id, stylist_id, menu_id,
				                          start_time, end_time, status)
				VALUES (?, ?, ?, ?, ?, ?, ?)
				""",
				id, customerId, stylistId, menuId,
				startTime, endTime, status.name());
		
		LoginUser user = new LoginUser(stylistId, "stylist@example.com");
		// 実行
		assertThatThrownBy(() -> stylistService.confirm(id, user))
		    .isInstanceOf(IllegalStateException.class)
		    .hasMessage("テスト用の失敗");
		
		// 検証
		String result = jdbcTemplate.queryForObject("""
				SELECT status FROM reservations WHERE id = 100
				""", String.class);
		
		assertThat(result).isEqualTo(Status.PENDING.name());
	}
	
	static class FailingReservationListener {
		private JdbcTemplate jdbcTemplate;
		
		public FailingReservationListener(JdbcTemplate jdbcTemplate) {
			this.jdbcTemplate = jdbcTemplate;
		}
		@EventListener
		public void onReservation(Reservation reservation) {
			
			// 例外発生前に更新後値を検証
			String status = jdbcTemplate.queryForObject("SELECT status FROM reservations WHERE id = ?", String.class, reservation.getId());
			assertThat(status).isEqualTo(Status.CONFIRMED.name());
			
			throw new IllegalStateException("テスト用の失敗");
		}
	}
	
	@TestConfiguration(proxyBeanMethods = false)
	static class FailureConfig {
		
		@Autowired
		private JdbcTemplate jdbcTemplate;
			
		@Bean
		FailingReservationListener failingReservationListener() {
			return new FailingReservationListener(this.jdbcTemplate);
		}
	}
	@AfterEach
	void cleanup() {
		jdbcTemplate.update("""
				DELETE FROM reservations WHERE id = ?
				""", 100L);
	}

}
