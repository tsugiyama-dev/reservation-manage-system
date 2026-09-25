package com.example.demo.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReviewPostForm;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Review;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class ReViewService {

	AuthenticationService authenticationService;
	ReservationRepository reservationRepository;
	ReviewRepository reviewRepository;
	
	public void post(String email, ReviewPostForm form, long reservationId) {
		
		Id<Reservation> rid = new Id<>(reservationId);
		Reservation reservation = reservationRepository.findByReservationId(rid).orElseThrow(() -> {
			throw new NoSuchElementException("予約が見つかりません");
		});
		
		try {
			authenticationService.authenticate(email, reservation.getCustomerId());
		}catch(AccessDeniedException ex) {
			throw new AccessDeniedException("");
		}catch(NoSuchElementException ex) {
			throw new NoSuchElementException("");
		}
		
		boolean isConfirmed = reservation.getStatus().equals(Status.CONFIRMED);
		if(!isConfirmed) {
			throw new IllegalStateException("確定されたステータス以外は投稿できません");
			}
		if(LocalDateTime.now().isBefore(reservation.getEndTime())) {
			throw new IllegalStateException("予約時間より前にレビューを投稿することはできません");
	    }
		 try {
			 reviewRepository.insert(reservationId, form);
		 }catch(Exception e) {
			 throw new SqlErrorException("レビューは既に投稿されています", e);
		 }
	}

	public StylistEvaluateResult getReview(long stylistId) {
		
		Id<Stylist> sid = new Id<>(stylistId);
		Review reviews = reviewRepository.findEvaluationByStylistId(sid);
		List<Review> comment = reviewRepository.findCommentByStylistId(sid);
		List<String> comments = comment.stream().filter(c -> c.getComment() != null).map(Review::getComment).toList();
		StylistEvaluateResult result = new StylistEvaluateResult(reviews.getReviewCount(), reviews.getReviewRating(), comments);
		
		return result;
		
	}
}
