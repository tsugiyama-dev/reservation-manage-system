package com.example.demo.service;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import jakarta.annotation.Nullable;

import org.springframework.stereotype.Service;

import com.example.demo.domain.TimeRange;
import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.StylistResponse;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.repository.BusinessHourRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class StylistMenuService {

	private final StylistMenuRepository stylistMenuRepository;
	private final ReservationRepository reservationRepository;
	private final BusinessHourRepository businessHourRepository;
	private final StylistRepository stylistRepository;

	public List<Menu> getMenuByStylist(long id) {
		return stylistMenuRepository.findAll(new Id<Stylist>(id));
		
	}
	public List<StylistResponse> getStylists() {
		var users = stylistRepository.findAll();
		return users.parallelStream().map(user -> new StylistResponse(
				user.getId(),
				user.getName(),
				user.getEmail(),
				user.getStylist().getBio())).toList();
	}

	public List<TimeRange> getAvalilableEmptySlot(
			Id<Stylist> sid, LocalDateTime targetDate,Id<Menu> mid,  @Nullable Long reservationId) {
		
	    BusinessHour businessHours = findBusinessHoursById(sid, targetDate);
		var freeSlots = extractAvailableTimeSlots(
				                createFreeSlot(businessHours),                 
				                findStylistReservations(sid, targetDate, reservationId));
		Menu menu = getMenuById(stylistMenuRepository.findAll(sid), mid);
		
		return generateCandidateSlot(freeSlots, menu.getBaseDurationMinutes());
	}
	
	
	private List<TimeRange> createFreeSlot(BusinessHour date) {
		LocalTime startTime = LocalTime.parse(date.startTime());
		LocalTime endTime = LocalTime.parse(date.endTime());

		return new ArrayList<TimeRange>(List.of(new TimeRange(startTime, endTime)));
	}
	private List<TimeRange> generateCandidateSlot(List<TimeRange> freeSlot, int menuDuration) {
		List<TimeRange> candidate = new ArrayList<>();
		
		for(TimeRange free: freeSlot) {
			LocalTime start = free.getStartTime();
			while(!start.plusMinutes(menuDuration).isAfter(free.getEndTime())) {
				candidate.add(new TimeRange(start, start.plusMinutes(menuDuration)));
				start = start.plusMinutes(30);
			}
		}
		return candidate;
	}
	private BusinessHour findBusinessHoursById(Id<Stylist> sid, LocalDateTime date) {
		return findWorkingDay(businessHourRepository.findById(sid), date);
	
	}
	
	private BusinessHour findWorkingDay(List<BusinessHour> businessHoursOfStylist, LocalDateTime date) {
		return  businessHoursOfStylist.stream()
				.filter(b -> b.dayOfWeek().toUpperCase().equals(date.getDayOfWeek().toString().subSequence(0, 3)))
				.findFirst().orElseThrow(() -> new RuntimeException("スタイリストの営業日を確認してください: 日付=" + date));
	}
	
	private List<Reservation> findStylistReservations(
			Id<Stylist> sid, LocalDateTime targetDate, @Nullable Long reservationId) {
		LocalDateTime start = targetDate.toLocalDate().atTime(LocalTime.MIN);
		LocalDateTime end = targetDate.toLocalDate().atTime(LocalTime.MAX);
		
		return 	reservationRepository.findByStylistId(sid, start, end, reservationId);
	}
	private List<TimeRange> extractAvailableTimeSlots(List<TimeRange> freeSlot, List<Reservation> reservations) {
		for(var reservation : reservations) {
			freeSlot = subtract(freeSlot, createTimeRangeFromReservation(reservation));
		}
		return freeSlot;
	}
	
	private TimeRange createTimeRangeFromReservation(Reservation reservation) {
		return new TimeRange(
				reservation.getStartTime().toLocalTime(),
				reservation.getEndTime().toLocalTime());
	}
	
	private List<TimeRange> subtract(List<TimeRange> freeSlot,  TimeRange reserve) {

		boolean isOk = false;
		List<TimeRange> candidate = new ArrayList<>();
		for(TimeRange free : freeSlot) {
			
			if(isOk) {
				candidate.add(free);
				continue;
			}
			// 空きスロットの開始時間に等しい場合
			if(free.getStartTime().equals(reserve.getStartTime()) &&
				reserve.getEndTime().isBefore(free.getEndTime())) {
				candidate.add(new TimeRange(reserve.getEndTime(),free.getEndTime()));
				isOk = true;
				continue;
			}
			// 空きスロットの開始時間と終了時間の間にある場合
			if(free.getStartTime().isBefore(reserve.getStartTime()) 
					&& reserve.getEndTime().isBefore(free.getEndTime())) {
				candidate.add(new TimeRange(free.getStartTime(), reserve.getStartTime()));
				candidate.add(new TimeRange(reserve.getEndTime(), free.getEndTime()));
				isOk = true;
				continue;
			
			}
			// 空きスロットと等しい場合
			if(free.getStartTime().equals(reserve.getStartTime()) &&
					reserve.getEndTime().equals(free.getEndTime())) {
				isOk = true;
				continue;
				// 候補時間を追加しない
			}
			// 空きスロットの終了時間が等しい場合
			if(free.getStartTime().isBefore(reserve.getStartTime()) &&
					reserve.getEndTime().equals(free.getEndTime())) {
				candidate.add(new TimeRange(free.getStartTime(), reserve.getStartTime()));
				isOk = true;
				continue;
			}
			if(!isOk) {
				candidate.add(free);
			}
		}
		return candidate;
	}
	 private Menu getMenuById(List<Menu> menus, Id<Menu> menuId) {
		return menus.stream().filter(m -> m.getId() == menuId.id()).findFirst()
				.orElseThrow(() -> new NoSuchElementException("指定されたメニューが見つかりません: メニューID=" + menuId.id()));
		
	}
}
