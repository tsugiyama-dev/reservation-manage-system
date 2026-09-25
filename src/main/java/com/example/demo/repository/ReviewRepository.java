package com.example.demo.repository;

import java.util.List;

import org.apache.ibatis.annotations.Insert;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Result;
import org.apache.ibatis.annotations.Results;
import org.apache.ibatis.annotations.Select;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReviewPostForm;
import com.example.demo.domain.entity.Review;
import com.example.demo.domain.entity.Stylist;

@Mapper
public interface ReviewRepository {

	@Insert("""
			INSERT INTO reviews (reservation_id, rating, comment)
			VALUES (#{reservationId}, #{form.rating}, #{form.comment})
			""")
	void insert(long reservationId, ReviewPostForm form);
	
	
	@Results(value = {
			@Result(property = "id", column = "user_id"),
			@Result(property = "reviewCount", column = "review_count"),
			@Result(property = "reviewRating", column = "review_avarage")
	})
	@Select("""
			SELECT 
			       s.user_id,
			       COUNT(rev.id) as review_count,
			       AVG(rev.rating) as review_avarage
			FROM stylists as s
			LEFT JOIN reservations as res 
			    ON s.user_id = res.stylist_id 
			LEFT JOIN reviews as rev
			    ON res.id = rev.reservation_id
			WHERE s.user_id = #{id}
			GROUP BY s.user_id
			""")
	Review findEvaluationByStylistId(Id<Stylist> id);

	@Results(value = {
			@Result(property = "id", column = "user_id"),
			@Result(property = "comment", column = "comment")
			})
	@Select("""
			SELECT 
			       s.user_id,
			       rev.comment
			FROM stylists as s
			LEFT JOIN reservations as res 
			    ON s.user_id = res.stylist_id 
			LEFT JOIN reviews as rev
			    ON res.id = rev.reservation_id
			WHERE s.user_id = #{id}
			""")
	List<Review> findCommentByStylistId(Id<Stylist> id);
}
