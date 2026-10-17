package com.example.demo.repository;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

@SpringBootTest
@Import(TestcontainersConfiguration.class)// 別packageにあるので@Importでインポートする
@Transactional
class ReservationRepositoryTest {

	@Autowired
	ReservationRepository reservationRepository;
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Test
	void 更新対象のレコードが正常に更新される() {
		// Arrange
		jdbcTemplate.update("""
				INSERT INTO reservations
				  (id, customer_id, stylist_id, menu_id,
				  start_time, end_time, status)
				VALUES 
				  (?, ?, ?, ?, ?, ?, ?),
				  (?, ?, ?, ?, ?, ?, ?)
				""",
				//1件
				100L, 1L, 2L, 3L,
				LocalDateTime.of(2026, 10, 13, 10, 0),
				LocalDateTime.of(2026, 10, 13, 11, 0),
				Status.PENDING.name(),
				//2件
				101L, 1L, 2L, 3L,
				LocalDateTime.of(2026, 10, 13, 14, 0),
				LocalDateTime.of(2026, 10, 13, 15, 0),
				Status.CONFIRMED.name());
		int before = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class);
		Reservation reservation = new Reservation();
		reservation.setId(100L);
		reservation.setStatus(Status.PENDING);
		reservation.setStartTime(LocalDateTime.of(2026, 10, 13, 12, 0));
		reservation.setEndTime(LocalDateTime.of(2026, 10, 13, 13, 0));
		
		// Act
		int updatedCount = reservationRepository.update(reservation);
		
		// Assert
		int after = jdbcTemplate.queryForObject("SELECT COUNT(*) FROM reservations", Integer.class);
		assertThat(after).isEqualTo(before);
		assertThat(updatedCount).isEqualTo(1);
		Map<String, Object> updated = jdbcTemplate.queryForMap("""
				SELECT * FROM reservations WHERE id = ?
				""", 100L);
		assertThat(updated.get("status")).isEqualTo(Status.PENDING.name());
		assertThat(updated.get("start_time")).isEqualTo(LocalDateTime.of(2026, 10, 13, 12, 0));
		assertThat(updated.get("end_time")).isEqualTo(LocalDateTime.of(2026, 10, 13, 13, 0));
		Map<String, Object> noUpdated = jdbcTemplate.queryForMap("""
				SELECT * FROM reservations WHERE id = ?
				""", 101L);
		assertThat(noUpdated.get("status")).isEqualTo(Status.CONFIRMED.name());
		assertThat(noUpdated.get("start_time")).isEqualTo(LocalDateTime.of(2026, 10, 13, 14, 0));
		assertThat(noUpdated.get("end_time")).isEqualTo(LocalDateTime.of(2026, 10, 13, 15, 0));
	}
}
