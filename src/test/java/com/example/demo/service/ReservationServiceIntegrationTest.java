package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.example.demo.TestcontainersConfiguration;
import com.example.demo.config.LoginUser;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Stylist;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
class ReservationServiceIntegrationTest {

	@Autowired
	JdbcTemplate jdbcTemplate;
	@Autowired
	ReservationService reservationService;
	
	@Test
	void マルチスレッドによる同時予約処理のテスト() throws Exception{
		
		long customerId = 1L;
		LoginUser user = new LoginUser(customerId, "customer@example.com");
		LocalDateTime reservationDate = LocalDateTime.of(2026, 10, 10, 10, 0, 0);
	    long stylistId = 2L;
	    long menuId = 3L;
		ReservationRequest<Stylist, Menu> req = new ReservationRequest<>(
				new Id<Stylist>(stylistId), reservationDate, new Id<Menu>(menuId));
		ExecutorService executors = Executors.newFixedThreadPool(2);
		CountDownLatch ready = new CountDownLatch(2);
		CountDownLatch start = new CountDownLatch(1);
		Callable<String> task = () -> {
			ready.countDown();
			if(!start.await(5, TimeUnit.SECONDS)) {
				throw new IllegalStateException("");
			}
			reservationService.reserve(req, user);
			return Thread.currentThread().getName();
		};
		try {
			Future<String> result1 = executors.submit(task);
			Future<String> result2= executors.submit(task);
			
			assertThat(ready.await(5, TimeUnit.SECONDS)).isTrue();
			
			start.countDown();
			int successCount = 0;
			int errorCount = 0;
			for(Future<String> future: List.of(result1, result2)) {
				try {
					future.get(10, TimeUnit.SECONDS);
					successCount++;
				}catch(ExecutionException  e) {
				    errorCount++;
				    assertThat(e.getCause())
				        .isInstanceOf(IllegalStateException.class)
				        .hasMessage("予約可能な枠がありません");
				}
			}
			assertThat(successCount).isEqualTo(1);
			assertThat(errorCount).isEqualTo(1);
			long rowCount = jdbcTemplate.queryForObject("""
					
					SELECT count(*)
					FROM reservations 
					WHERE customer_id = ?
					    AND stylist_id  = ?
					    AND menu_id = ?
					    AND start_time = ?
					    """,
					Long.class,
					customerId, stylistId, menuId, reservationDate);
			assertThat(rowCount).isEqualTo(1);
			
		}finally {
			executors.shutdownNow();
			jdbcTemplate.update("""
					DELETE FROM reservations
					WHERE customer_id = 1
					  AND stylist_id = 2
					  AND menu_id = 3
					  AND start_time = '2026-10-10 10:00:00'
					""");
		}
	}
}
