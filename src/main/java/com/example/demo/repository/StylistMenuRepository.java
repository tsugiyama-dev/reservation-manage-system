package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Stylist;

@Mapper
public interface StylistMenuRepository {

	@Insert("""
			INSERT INTO stylists_menus (
			    stylist_id,
			    menu_id,
			    duration_minutes,
			    price)
			VALUES (
			    #{stylistId},
			    #{menuId},
			    #{durationMinutes},
			    #{price})
			ON DUPLICATE KEY UPDATE
			  duration_minutes = VALUES(duration_minutes),
			  price = VALUES(price)
			""")
	void insert(long stylistId, long menuId, int durationMinutes, int price);

	@Results(id = "menuResultMap", value = {
			@Result(property = "id", column = "menu_id"),
			@Result(property = "name", column = "name"),
			@Result(property = "baseDurationMinutes", column = "duration_minutes"),
			@Result(property = "basePrice", column = "price")
	})
	@Select("""
			SELECT m.name as name,
			       sm.menu_id as menu_id,
			       sm.duration_minutes as duration_minutes,
			       sm.price as price
			FROM menus as m
			RIGHT JOIN stylists_menus as sm
			ON sm.menu_id = m.id
			WHERE sm.stylist_id = #{id}
			""")
	List<Menu> findAllMenu(Id<Menu> id);
	
	@ResultMap(value = "menuResultMap")
	@Select("""
			SELECT m.name as name,
			       sm.menu_id as menu_id,
			       sm.duration_minutes as duration_minutes,
			       sm.price as price
			FROM menus as m
			RIGHT JOIN stylists_menus as sm
			ON sm.menu_id = m.id
			WHERE sm.stylist_id = #{stylistId.id} AND
			      sm.menu_id = #{menuId.id}
			""")
	Menu findStylistMenuByStylistIdAndMenuId(Id<Stylist> stylistId, Id<Menu> menuId);
	
}
