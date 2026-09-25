package com.example.demo.repository;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Stylist;

@Mapper
public interface StylistRepository {

	@Select("""
			SELECT user_id FROM stylists WHERE user_id = #{id} FOR UPDATE
			""")
	long lockStylist(Id<Stylist> id);
	
//	@Options(useGeneratedKeys = true, keyProperty = "id")
	@Insert("""
			  INSERT INTO stylists (user_id, bio)
			  VALUES
		        (#{userId}, #{bio})
			""")
	void insert(long userId, String bio);

}
