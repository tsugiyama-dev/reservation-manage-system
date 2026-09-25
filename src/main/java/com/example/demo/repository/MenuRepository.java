package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Delete;
import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.RegisterMenuForm;
import com.example.demo.domain.entity.Menu;

@Mapper
public interface MenuRepository {

	@Insert("""
			INSERT INTO menus (name, base_duration_minutes, base_price)
			VALUES (#{name}, #{baseDurationMinutes}, #{price})
			""")
	void insert(RegisterMenuForm form);
	
	@Results(value= {
			@Result(property = "id", column = "id"),
			@Result(property = "name", column = "name"),
			@Result(property = "baseDurationMinutes", column = "base_duration_minutes"),
			@Result(property = "basePrice", column = "base_price"),
			
	})
	@Select("SELECT id, name, base_duration_minutes, base_price FROM menus WHERE id = #{id}")
	Optional<Menu> findById(Id<Menu> id);
	
	List<Menu> findAll(Id<Menu> id);
	@Results(
			value= {
		    @Result(property = "id", column = "id", id = true),
			@Result(property = "name", column = "name"),
			@Result(property = "baseDurationMinutes", column = "base_duration_minutes"),
			@Result(property = "basePrice", column = "base_price")
			})
	@Select("SELECT id, name, base_duration_minutes, base_price FROM menus WHERE name = #{name}")
	Menu findByName(String name);
	
	@Select("SELECT * FROM menus")
	List<Menu> findAllMenu();
	
	@Update("""
			<script>
			  UPDATE menus
			   <set>
			     <if test="form.name != null and form.name != ''">
			       name = #{form.name}
			     </if>
			     <if test="form.baseDurationMinutes != 0">
			       base_duration_minutes = #{form.baseDurationMinutes}
			     </if>
			     <if test="form.basePrice != 0">
			       base_price = #{form.basePrice}
			     </if>
			   </set>
			  </script>
			  """)
	void update(RegisterMenuForm form);
	
	@Delete("DELETE FROM menus WHERE id = #{menuId}")
	void delete(Id<Menu> menuId);
	
}
