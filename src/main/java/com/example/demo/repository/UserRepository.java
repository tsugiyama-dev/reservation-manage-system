package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.UserForm;
import com.example.demo.domain.entity.User;

@Mapper
public interface UserRepository {

	@Options(useGeneratedKeys = true, keyProperty = "id")
	@Insert("""
			   INSERT INTO users (name, password_hash, email, role)
			   VALUES (#{name}, #{password_hash}, #{email}, #{role})
			""")
	long insert(User user);
	
	@Insert("INSERT INTO stylists (user_id, bio, created_at)"
			+ "VALUES (#{userId}, #{bio}, #{createdAt}")
	long insertStylist(long userId, String bio, LocalDateTime createdAt);
	
	
	@Update("UPDATE users SET username = #{username}, password = #{password}, email = #{email}, role = #{role} WHERE id = #{id}")
	User update(UserForm user);
	
	@Delete("DELETE FROM users WHERE id = #{id}")
	long delete(Long id);
	
	@Select("SELECT * FROM users WHERE name = #{username}")
	Optional<User> findByUsername(String username);

	@Results(id = "userResultMap", value = {
			@Result(property = "id", column = "id"),
			@Result(property = "name", column = "name"),
			@Result(property = "email", column = "email"),
			@Result(property = "role", column = "role")
	})
	@Select("SELECT id, name, email, role FROM users WHERE role = #{role}")
	List<User> findByRole(String role);
	
	@ResultMap(value = "userResultMap")
	@Select("SELECT id, name, email, role FROM users WHERE id = #{id}")
	Optional<User> findById(Id<User> uid);
	
	
}
