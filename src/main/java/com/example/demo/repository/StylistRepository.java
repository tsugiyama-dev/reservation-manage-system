package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;

@Mapper
public interface StylistRepository {

	@Select("""
			SELECT user_id FROM stylists WHERE user_id = #{id} FOR UPDATE
			""")
	long lockStylist(Id<Stylist> id);
	
	@Insert("""
			  INSERT INTO stylists (user_id, bio)
			  VALUES (#{userId}, #{bio})
			""")
	void insert(long userId, String bio);
	
	@Select("SELECT id FROM stylists WHERE user_id = #{id}")
	Optional<Stylist> findById(Id<Stylist> id);

	@Delete("DELETE FROM stylists WHERE user_id = #{id}")
	void delete(Id<Stylist> id);
	
	@Results(value = {
			@Result(property = "id", column = "id"),
			@Result(property = "name", column = "name"),
			@Result(property = "email", column = "email"),
			@Result(property = "passwordHash", column = "password_hash"),
			@Result(property = "role", column = "role"),
			@Result(property = "createdAt", column = "created_at"),
			
			@Result(property = "stylist.bio", column = "bio")
	})
	@Select("""
			SELECT 
			      u.id,
			      u.name,
			      u.email,
			      u.password_hash,
			      u.role,
			      u.created_at,
			      s.bio
			FROM stylists s
			LEFT JOIN users u
			  ON s.user_id = u.id
			WHERE role = "STYLIST"
			""")
	List<User> findAll();

}
