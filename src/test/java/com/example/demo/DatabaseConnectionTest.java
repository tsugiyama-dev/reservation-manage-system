package com.example.demo;

import static org.assertj.core.api.Assertions.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@SpringBootTest
@Import(TestcontainersConfiguration.class)
public class DatabaseConnectionTest {
	
	@Autowired
	JdbcTemplate jdbcTemplate;
	
	@Test
	void MySQLに接続できる() {
		Integer result = 
				jdbcTemplate.queryForObject("SELECT 1", Integer.class);
		
		assertThat(result).isEqualTo(1);
	}

}
