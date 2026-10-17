package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.function.Supplier;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.TimeRange;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.User;
import com.example.demo.domain.entity.impl.ReservationStatusResolver;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class ReservationService {
	
	private AuthenticationService authenticationService;
	private UserRepository userRepository;
	private StylistMenuService stylistMenuService;
	private StylistMenuRepository stylistMenuRepository;
	private StylistRepository stylistRepository;
	private ReservationRepository reservationRepository;
	private ReservationStatusResolver reservationStatusResolver;
	
	@Transactional
	public void reserve(ReservationRequest<Stylist, Menu> req, LoginUser user) {
	
		
		stylistRepository.lockStylist(req.stylistId()); // Lock獲得操作
	
		Menu menu = findMenuById(req.stylistId(), req.menuId());
		LocalTime updatedStart = LocalTime.of(req.date().getHour(), req.date().getMinute());
		LocalTime updatedEnd = updatedStart.plusMinutes(menu.getBaseDurationMinutes());
		TimeRange timeRange = new TimeRange(updatedStart, updatedEnd);

		List<TimeRange> emptyRanges = stylistMenuService.getAvalilableEmptySlot(req.stylistId(), req.date(),req.menuId(), null);
				
		isAvailableWithinSlot(emptyRanges, timeRange);

		Reservation reservation = new Reservation();
		reservation.setCustomerId(user.id());
		reservation.setStylistId(req.stylistId().id());
		reservation.setMenuId(req.menuId().id());
		reservation.setStartTime(req.date());
		reservation.setEndTime(req.date().plusMinutes(menu.getBaseDurationMinutes()));
		reservation.setStatus(Status.PENDING);
		
		reservationRepository.insert(reservation);
	}
	
	public void cancel(long rid, LoginUser user) {
		
		Reservation reservation = getReservationById(rid);
		authenticationService.identify(user, reservation.getCustomerId());
		reservationStatusResolver.getInstance(reservation.getStatus()).cancel(reservation);
		reservationRepository.update(reservation);
	}

	public List<Reservation> getReservations(LoginUser user) {
		isActiveUser(user.id());
		return reservationRepository.findByUserId(new Id<User>(user.id()));
	}

	@Transactional
	public void change(LoginUser user, long reservationId, ReservationRequest<Stylist, Menu> change) {
		
		Reservation reservation = getReservationById(reservationId);
		authenticationService.identify(user, reservation.getCustomerId());
		
		Id<Stylist> sid = new Id<Stylist>(reservation.getStylistId());
		Id<Menu> mid = new Id<Menu>(reservation.getMenuId());
		stylistRepository.lockStylist(sid); // Lock獲得
		Menu menu = findMenuById(sid, mid);
		TimeRange treatmentTimeRange = createTimeRange(change.date(), menu);
		List<TimeRange> emptyTimeSlots = stylistMenuService.getAvalilableEmptySlot(sid, change.date(), mid, reservationId);
		isAvailableWithinSlot(emptyTimeSlots, treatmentTimeRange);
		createEntity(reservation, change.date(), treatmentTimeRange);

		reservationRepository.update(reservation);
	}
	
	private void createEntity(Reservation reservation, LocalDateTime change, TimeRange treatmentTimeRange) {

		reservationStatusResolver.getInstance(reservation.getStatus()).pending(reservation);
		reservation.setStartTime(change.withHour(treatmentTimeRange.getStartTime().getHour())
                                       .withMinute(treatmentTimeRange.getStartTime().getMinute()));
        reservation.setEndTime(change.withHour(treatmentTimeRange.getEndTime().getHour())
                                     .withMinute(treatmentTimeRange.getEndTime().getMinute()));
	}
	private TimeRange createTimeRange(LocalDateTime change, Menu menu) {
		LocalTime start = change.toLocalTime();
		LocalTime end = start.plusMinutes(menu.getBaseDurationMinutes());
		return new TimeRange(start, end);
	}
	private void isActiveUser(long userId) {
		userRepository.findById(new Id<User>(userId)).orElseThrow(userNotFoundExceptionSupplier(userId));
	}
	private Supplier<NoSuchElementException> userNotFoundExceptionSupplier(long id) {
		return () -> new NoSuchElementException("指定されたユーザーが見つかりません: ユーザーID=" + id);
	}
	private void isAvailableWithinSlot(List<TimeRange> emptySlots, TimeRange changeTimeRange) {
		emptySlots.stream().filter(emptySlot -> {
		return changeTimeRange.getStartTime().equals(emptySlot.getStartTime()) 
				       &&changeTimeRange.getEndTime().equals(emptySlot.getEndTime());
				}).findFirst().orElseThrow(reservationAvailabilitySlotNotFoundExceptionSupplier());
	}
	private Supplier<IllegalStateException> reservationAvailabilitySlotNotFoundExceptionSupplier() {
		return () -> new IllegalStateException("予約可能な枠がありません");
	}
	
	private Reservation getReservationById(long id) {
		return reservationRepository.findById(new Id<Reservation>(id))
				  .orElseThrow(reservationNotFoundExceptionSupplier(id));
	}
	private Supplier<NoSuchElementException> reservationNotFoundExceptionSupplier(long id) {
		return () -> new NoSuchElementException("指定された予約が見つかりません: 予約ID=" + id);
	}
	private Menu findMenuById(Id<Stylist> stylistId, Id<Menu> menuId) {
		var menus = stylistMenuRepository.findAll(stylistId);
		return menus.stream().filter(m -> m.getId() == menuId.id()).findFirst()
				.orElseThrow(menuNotFoundExceptionSupplier(menuId.id()));
	}
	private Supplier<NoSuchElementException> menuNotFoundExceptionSupplier(long id) {
		return () -> new NoSuchElementException("存在しないメニューＩＤです。スタイリストのメニューをご確認ください");
	}
}
