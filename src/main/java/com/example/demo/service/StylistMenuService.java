package com.example.demo.service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;

import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.StylistOriginalMenu;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.StylistMenu;
import com.example.demo.repository.BusinessHourRepository;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class StylistMenuService {

	private StylistMenuRepository stylistMenuRepository;
	private MenuRepository menuRepository;
	private ReservationRepository reservationRepository;
	private BusinessHourRepository businessHourRepository;
	
	public void assignMenu(long stylistId, long menuId, StylistOriginalMenu original) {
		Id<Menu> id = new Id<>(menuId);
		Menu m = menuRepository.findById(id).orElseThrow(
				() -> {
					throw new IllegalArgumentException("メニューが存在しません。[id=" + menuId + "]");
				});
		stylistMenuRepository.insert(
				new StylistMenu(
						stylistId,
						menuId,
						original.duration_minutes() != null ?
								original.duration_minutes() :
									m.getBaseDurationMinutes(),
						original.price() != null ?
								original.price() :
									m.getBasePrice()));
	}

	public List<Menu> getMenuForStylist(long id) {
		return stylistMenuRepository.findAllMenu(new Id<Menu>(id));
		
	}

	public List<TimeRange> getList(long stylistId, LocalDateTime date, long menuId) {
		
		String dayOfWeek = date.getDayOfWeek().toString().substring(0, 3); // 曜日
		LocalDate day = LocalDate.of(date.getYear(), date.getMonth(), date.getDayOfMonth()); // 日にち
		
		// スタイリストの営業日を取り出す
		List<BusinessHour> businessDays = businessHourRepository.findById(new Id<BusinessHour>(stylistId));
		
		Optional<BusinessHour> result = businessDays.stream()
				.filter(b -> b.dayOfWeek().toUpperCase().equals(dayOfWeek)).findFirst();
		
		if(result.isEmpty()) {
			return List.of();
		}
		BusinessHour businessHour = result.get();
		// 初期値は営業開始～営業終了を空き時間としてリストを作成
		List<TimeRange> freeSlot = new ArrayList<>();
		freeSlot.add(new TimeRange(LocalTime.parse(businessHour.startTime()),
				                   LocalTime.parse(businessHour.endTime())));
		
		// 予約一覧を取り出す
		Id<StylistOriginalMenu> id = new Id<>(stylistId);
		List<Reservation> reservations = reservationRepository.findByStylistId(
				id,
				day.atTime(LocalTime.parse(businessHour.startTime())),
				day.atTime(LocalTime.parse(businessHour.endTime()))
				);
		
		// 既存予約を差し引いた残りの空き時間を取り出す
		for(var reservation : reservations) {
			freeSlot = subtract(freeSlot
					, new TimeRange(
							LocalTime.of(reservation.getStartTime().getHour(), reservation.getStartTime().getMinute()),
							LocalTime.of(reservation.getEndTime().getHour(), reservation.getEndTime().getMinute())));
		}
		
		Menu menu = stylistMenuRepository.findStylistMenuByStylistIdAndMenuId(new Id<Stylist>(stylistId), new Id<Menu>(menuId));
		log.info("menu={}", menu);
		List<TimeRange> candidate = new ArrayList<>();
		
		for(TimeRange free: freeSlot) {
			LocalTime start = free.getStartTime();
			while(start.plusMinutes(menu.getBaseDurationMinutes()).isBefore(free.getEndTime()) ||
					!start.plusMinutes(menu.getBaseDurationMinutes()).isAfter(free.getEndTime())) {
				candidate.add(new TimeRange(start, start.plusMinutes(menu.getBaseDurationMinutes())));
				start = start.plusMinutes(30);
			}
		}
		return candidate;
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
}
