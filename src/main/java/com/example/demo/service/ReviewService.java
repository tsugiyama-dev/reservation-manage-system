package com.example.demo.service;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReviewPostForm;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Review;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ReviewService {

	private final AuthenticationService authenticationService;
	private final ReservationRepository reservationRepository;
	private final ReviewRepository reviewRepository;
	
	public void post(LoginUser user, ReviewPostForm form, long reservationId) {
		
		Reservation reservation = getReservation(new Id<Reservation>(reservationId));
		authenticationService.identify(user, reservation.getCustomerId());
	
		if(!reservation.getStatus().equals(Status.COMPLETED))
			throw new IllegalStateException("statusがCOMPLETEDのみレビューを投稿できます");
		reviewRepository.insert(reservationId, form);
	}

	public StylistEvaluateResult getReview(long stylistId) {
		
		Id<Stylist> sid = new Id<>(stylistId);
		Review reviews = reviewRepository.findEvaluationByStylistId(sid);
		List<Review> comment = reviewRepository.findCommentByStylistId(sid);
		List<String> comments = comment.stream().filter(c -> c.getComment() != null).map(Review::getComment).toList();
		StylistEvaluateResult result = new StylistEvaluateResult(reviews.getReviewCount(), reviews.getReviewRating(), comments);
		
		return result;
		
	}
	private Reservation getReservation(Id<Reservation> rid) {
		return reservationRepository.findById(rid).orElseThrow(reservationNotFoundSupplier(rid));
	}
	private Supplier<NoSuchElementException> reservationNotFoundSupplier(Id<Reservation> rid) {
		return () -> new NoSuchElementException("予約が見つかりません: 予約ID=" + rid);
	}
}
