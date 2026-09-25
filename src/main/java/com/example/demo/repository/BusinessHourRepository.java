package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.Id;

@Mapper
public interface BusinessHourRepository {

	@Insert("INSERT INTO business_hour (stylist_id, day_of_week, start_time, end_time)"
			+ "VALUES (#{stylistId}, #{businessHours.dayOfWeek}, #{businessHours.startTime}, #{businessHours.endTime})")
	void insert(long stylistId, BusinessHour businessHours);
	void update(List<BusinessHour> businessHours);
	
	@Delete("DELETE FROM business_hour WHERE stylist_id = #{stylistId}")
	void delete(long stylistId);
	
	@Select("SELECT day_of_week, start_time, end_time FROM business_hour "
			+ "WHERE stylist_id = #{id}")
	List<BusinessHour> findById(Id<BusinessHour> id);
}
