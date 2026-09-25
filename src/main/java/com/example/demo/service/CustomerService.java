package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class CustomerService {
	
	StylistMenuService stylistMenuService;
	StylistMenuRepository stylistMenuRepository;
	StylistRepository stylistRepository;
	ReservationRepository reservationRepository;
	AuthenticationService authenticationService;
	
	@Transactional
	public void tentativeReserve(long stylistId, LocalDateTime date, long menuId, long userId) {
	
		Id<Stylist> sid = new Id<>(stylistId);
		Id<Menu> mid = new Id<>(menuId);
		Id<User> uid = new Id<>(userId);
		
		stylistRepository.lockStylist(sid); // Lock獲得操作
	
		Menu menu = stylistMenuRepository.findStylistMenuByStylistIdAndMenuId(sid, mid);
		LocalTime trans = LocalTime.of(date.getHour(), date.getMinute());
		LocalTime end = trans.plusMinutes(menu.getBaseDurationMinutes());
		TimeRange timeRange = new TimeRange(LocalTime.of(date.getHour(), date.getMinute()),
				                            LocalTime.of(end.getHour(), end.getMinute()));

		// 空き時間の再チェック
		List<TimeRange> emptyRanges = stylistMenuService.getList(stylistId, date, menuId);
		Optional<TimeRange> result = emptyRanges.stream().
				filter(range -> {
					return timeRange.getStartTime().equals(range.getStartTime()) &&
				    timeRange.getEndTime().equals(range.getEndTime());
				}).findFirst();
		if(result.isEmpty()) {
			throw new IllegalStateException("既に予約が入っています");
		}
		reservationRepository.insert(uid, sid, mid, date, date.plusMinutes(menu.getBaseDurationMinutes()), Status.PENDING, LocalDateTime.now());
	}


	public void cancel(long reservationId, String email) {
		Reservation reservation = reservationRepository.findByReservationId(new Id<Reservation>(reservationId)).orElseThrow(() -> {
			throw new NoSuchElementException("指定された予約が見つかりません");
		});
		
		try {
			authenticationService.authenticate(email, reservation.getCustomerId());
		}catch(NoSuchElementException e) {
			throw new NoSuchElementException("ユーザーが見つかりません");
		}catch(AccessDeniedException e) {
			throw new AccessDeniedException("指定された予約を操作できません");
		}
		
		boolean isConfirmed = reservation.getStatus().equals(Status.CONFIRMED);
		boolean isPending = reservation.getStatus().equals(Status.PENDING);
		if(!isConfirmed && !isPending) {
			throw new IllegalStateException("保留中もしくは確定された予約しか操作できません");
		}
		
		reservation.setStatus(Status.CANCELLED);
		reservationRepository.update(reservation);
	}

	

}
