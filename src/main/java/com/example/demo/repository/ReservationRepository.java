package com.example.demo.repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Options;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.ResultMap;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;
import org.apache.ibatis.annotations.Update;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
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
			<script>
				SELECT 
				       id,
				       customer_id,
				       stylist_id,
				       menu_id,
				       start_time,
				       end_time,
				       status
				FROM reservations
				WHERE stylist_id = #{stylist.id} AND
				      start_time &gt;= #{startTime} AND
				      end_time &lt;= #{endTime} AND
				      status IN ('PENDING', 'CONFIRMED')
				      <if test="reservationId != null">
				        AND id != #{reservationId}
				      </if>
			    ORDER BY start_time ASC
			</script>
			""")
	List<Reservation> findByStylistId(
			Id<Stylist> stylist,
			LocalDateTime startTime,
			LocalDateTime endTime,
			Long reservationId);

	@Options(useGeneratedKeys = true, keyProperty = "id")
	@Insert("""
			INSERT INTO reservations (
			    customer_id,
			    stylist_id,
			    menu_id,
			    start_time,
			    end_time,
			    status)
			VALUES (
			    #{customerId},
			    #{stylistId},
			    #{menuId},
			    #{startTime},
			    #{endTime},
			    #{status})
			""")
	void insert(Reservation reservation);
	
	@ResultMap(value = "reservationResultMap")
	@Select("SELECT * FROM reservations WHERE id = #{id}")
	Optional<Reservation> findById(Id<Reservation> rid);

	@Update("""
			<script>
			  UPDATE reservations 
			  <set>
			      <if test="status != null">
				    status = #{status},
			      </if>
			      <if test="startTime != null">
			        start_Time = #{startTime},
			      </if>
			      <if test="endTime != null">
			        end_Time = #{endTime}
			      </if>
			  </set>
			  WHERE id = #{id}
		    </script>
			""")
	int update(Reservation reservation);

	@ResultMap(value = "reservationResultMap")
	@Select("""
			SELECT id,
			       customer_id,
			       stylist_id,
			       menu_id,
			       start_time,
			       end_time,
			       status
			FROM reservations
			WHERE status = #{status} AND
			      stylist_id = #{id.id}""")
	List<Reservation> findByStatus(Id<User> uid, Status status);

	@ResultMap(value = "reservationResultMap")
	@Select("""
			SELECT id,
			       customer_id,
			       stylist_id,
			       menu_id,
			       start_time,
			       end_time,
			       status
			FROM reservations
			WHERE customer_id = #{id}
			""")
	List<Reservation> findByUserId(Id<User> uid);

}
