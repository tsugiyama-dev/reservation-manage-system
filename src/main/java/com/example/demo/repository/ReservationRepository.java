package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.StylistOriginalMenu;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;

@Mapper
public interface ReservationRepository {

	
	@Results(id = "reservationResultMap", value = {
			@Result(property = "id", column = "id"),
			@Result(property = "customerId", column = "customer_id"),
			@Result(property = "stylistId", column = "stylist_id"),
			@Result(property = "menuId", column = "menu_id"),
			@Result(property = "startTime", column = "start_time"),
			@Result(property = "endTime", column = "end_time"),
			@Result(property = "status", column = "status"),
	})
	@Select("""
			SELECT 
			       id,
			       customer_id,
			       stylist_id,
			       menu_id,
			       start_time,
			       end_time,
			       status
			FROM reservations
			WHERE stylist_id = #{id.id} AND
			      start_time >= #{startTime} AND
			      end_time <= #{endTime} AND
			      status IN ('PENDING', 'CONFIRMED')
		    ORDER BY start_time ASC
			""")
	List<Reservation> findByStylistId(Id<StylistOriginalMenu> id, LocalDateTime startTime, LocalDateTime endTime);

	@Insert("""
			INSERT INTO reservations (
			    customer_id,
			    stylist_id,
			    menu_id,
			    start_time,
			    end_time,
			    status,
			    created_at)
			VALUES (
			    #{cid.id},
			    #{sid.id},
			    #{mid.id},
			    #{startTime},
			    #{endTime},
			    #{status},
			    #{now})
			""")
	void insert(Id<User> cid, Id<Stylist> sid, Id<Menu> mid,
			LocalDateTime startTime, LocalDateTime endTime, Status status, LocalDateTime now);
	
	@ResultMap(value = "reservationResultMap")
	@Select("""
			SELECT * FROM reservations WHERE id = #{id}
			""")
	Optional<Reservation> findByReservationId(Id<Reservation> id);

	@Update("""
			UPDATE reservations SET status = #{status} WHERE id = #{id}
			""")
	void update(Reservation reservation);

}
