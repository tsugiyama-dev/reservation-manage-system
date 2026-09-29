package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class CustomerService {
	
	private UserRepository userRepository;
	private StylistMenuService stylistMenuService;
	private StylistMenuRepository stylistMenuRepository;
	private StylistRepository stylistRepository;
	private ReservationRepository reservationRepository;
	private ReservationRequestValidator requestValidator;
	
	@Transactional
	public void reserve(ReservationRequest<Stylist, Menu> req, String email) {
	
		User user = userRepository.findByEmail(email).orElseThrow(() -> new NoSuchElementException("ユーザーが見つかりません"));
		Id<Stylist> sid = req.stylistId();
		Id<Menu> mid = req.menuId();
		Id<User> uid = new Id<>(user.getId());
		
		stylistRepository.lockStylist(sid); // Lock獲得操作
	
		Menu menu = getMenu(stylistMenuRepository.findAllByStylistId(sid), req.menuId());
		LocalTime start = LocalTime.of(req.date().getHour(), req.date().getMinute());
		LocalTime end = start.plusMinutes(menu.getBaseDurationMinutes());
		TimeRange timeRange = new TimeRange(start, end);

		// 空き時間の再チェック
		List<TimeRange> emptyRanges = stylistMenuService.getList(req.stylistId(), req.date(), req.menuId());
		Optional<TimeRange> result = emptyRanges.stream().
				filter(range -> {
					return timeRange.getStartTime().equals(range.getStartTime()) &&
				    timeRange.getEndTime().equals(range.getEndTime());
				}).findFirst();
		if(result.isEmpty()) {
			throw new IllegalStateException("既に予約が入っています");
		}
		reservationRepository.insert(uid, sid, mid, req.date(), req.date().plusMinutes(menu.getBaseDurationMinutes()), Status.PENDING, LocalDateTime.now());
	}
	
	public void cancel(long rid, String email) {
		Reservation checked = requestValidator.cancelCheck(rid, email);
		reservationRepository.update(checked);
	}

	public List<Reservation> getReservationList(String email) {
		User user = userRepository.findByEmail(email).orElseThrow(() -> {
			throw new IllegalArgumentException("ユーザーが見つかりません");
		});
		return reservationRepository.findByUserId(new Id<User>(user.getId()));
		
	}


	@Transactional
	public void changeSchedule(String email, long reservationId, ReservationRequest<Stylist, Menu> change) {
		
		long userId = requestValidator.changeCheck(email, reservationId);
		
		Id<Stylist> sid = change.stylistId();
		Id<Menu> mid = change.menuId();
		Id<User> uid = new Id<>(userId);
		
		stylistRepository.lockStylist(sid); // Lock獲得操作
		
		Menu menu = getMenu(stylistMenuRepository.findAllByStylistId(sid), change.menuId());
		LocalTime start = LocalTime.of(change.date().getHour(), change.date().getMinute());
		LocalTime end = start.plusMinutes(menu.getBaseDurationMinutes());
		TimeRange timeRange = new TimeRange(start, end);
		
		// 空き時間の再チェック
		List<TimeRange> emptyRanges = stylistMenuService.getList(sid, change.date(), mid);
		Optional<TimeRange> result = emptyRanges.stream().
				filter(range -> {
					return timeRange.getStartTime().equals(range.getStartTime()) &&
				    timeRange.getEndTime().equals(range.getEndTime());
				}).findFirst();
		if(result.isEmpty()) {
			throw new IllegalStateException("既に予約が入っています");
		}
		
		reservationRepository.insert(uid,
				sid,
				mid,
				LocalDateTime.of(change.date().getYear(), change.date().getMonth(), change.date().getDayOfMonth(), start.getHour(), start.getMinute()),
				LocalDateTime.of(change.date().getYear(), change.date().getMonth(), change.date().getDayOfMonth(), end.getHour(), end.getMinute()),
				Status.PENDING,
				LocalDateTime.now());
	}
	
	 private Menu getMenu(List<Menu> menus, Id<Menu> menuId) {
		Optional<Menu> menu = menus.stream().filter(m -> m.getId() == menuId.id()).findFirst();
		if(menu.isEmpty()) {
			throw new NoSuchElementException("""
					指定したメニューはありません
					再度スタイリストのメニューをご確認ください
					""");
		}
		return menu.get(); 
	}

	

}
