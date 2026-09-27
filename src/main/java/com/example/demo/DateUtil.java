package com.example.demo;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class DateUtil {

	public static DateTimeFormatter formatter() {
		return DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
	}
	public static void toLocalDateTime(LocalTime time) {
		
	}
	
}
