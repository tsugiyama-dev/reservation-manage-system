package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.Collections;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.example.demo.domain.TimeRange;
import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.repository.BusinessHourRepository;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;

@ExtendWith(MockitoExtension.class)
class StylistMenuServiceTest {

	@Mock
	StylistMenuRepository stylistMenuRepository;
	@Mock
	BusinessHourRepository businessHourRepository;
	
	@Mock
	ReservationRepository reservationRepository;
	
	@InjectMocks
	StylistMenuService stylistMenuService;
	
	@Nested
	@DisplayName("予約可能な候補を生成する処理")
	class 予約可能な候補を生成する処理 {

		@Nested
		@DisplayName("正常系")
		class 正常系 {
			@Test
			void 既存予約を差し引いた候補が生成される() {
				
				long stylistId = 1L;
				List<BusinessHour> businessHours = generateBusinessHours();
				when(businessHourRepository.findById(new Id<Stylist>(stylistId))).thenReturn(businessHours);
				
				List<Reservation> reservations = generateReservations();
				LocalDateTime targetDate = LocalDateTime.of(2026, 10, 12, 13, 0, 0);
				var start = targetDate.toLocalDate().atTime(LocalTime.MIN);
				var end = targetDate.toLocalDate().atTime(LocalTime.MAX);
				when(reservationRepository.findByStylistId(new Id<Stylist>(stylistId), start, end, null)).thenReturn(reservations);
				
				Menu menu = new Menu();
				int menuId = 4;
				menu.setId(menuId);
				menu.setBaseDurationMinutes(60);
				when(stylistMenuRepository.findAll(new Id<Stylist>(stylistId))).thenReturn(List.of(menu));

				List<TimeRange> result = stylistMenuService.getAvalilableEmptySlot(
						new Id<Stylist>(stylistId), targetDate, new Id<Menu>(menuId), null);
				
				assertThat(result.size()).isEqualTo(5);
				assertThat(result)
				    .extracting(TimeRange::getStartTime, TimeRange::getEndTime)
				    .containsExactly(
				    		tuple(LocalTime.of(11, 0), LocalTime.of(12, 0)),
				    		tuple(LocalTime.of(11, 30), LocalTime.of(12, 30)),
				    		tuple(LocalTime.of(12, 0), LocalTime.of(13, 0)),
				    		tuple(LocalTime.of(12, 30), LocalTime.of(13, 30)),
				    		tuple(LocalTime.of(13, 0), LocalTime.of(14, 0)));			    		
			}
			
			private List<BusinessHour> generateBusinessHours() {
				return List.of(
						new BusinessHour("Mon", "10:00", "15:00"),
						new BusinessHour("Tue", "10:00", "11:00"),
						new BusinessHour("Wed", "10:00", "15:00"),
						new BusinessHour("Thu", "10:00", "11:00"),
						new BusinessHour("Sun", "11:00", "16:00"));
			}
			
			private List<Reservation> generateReservations() {
				Reservation reservation1 = new Reservation();
				reservation1.setStartTime(LocalDateTime.of(2026, 10, 12, 10, 0));
				reservation1.setEndTime(LocalDateTime.of(2026, 10, 12, 11, 00));				
				Reservation reservation2 = new Reservation();
				reservation2.setStartTime(LocalDateTime.of(2026, 10, 12, 14, 00));
				reservation2.setEndTime(LocalDateTime.of(2026, 10, 12, 15, 00));				
				
				return List.of(
						reservation1,
						reservation2);
			}
			@Test
			@DisplayName("既存予約がない場合の予約可能な候補")
			void 既存予約がない予約可能な候補() {
				long stylistId = 1L;
				List<BusinessHour> businessHours = generateBusinessHours();
				when(businessHourRepository.findById(new Id<Stylist>(stylistId))).thenReturn(businessHours);
				
				LocalDateTime targetDate = LocalDateTime.of(2026, 10, 12, 13, 0, 0);
				var start = targetDate.toLocalDate().atTime(LocalTime.MIN);
				var end = targetDate.toLocalDate().atTime(LocalTime.MAX);
				when(reservationRepository.findByStylistId(new Id<Stylist>(stylistId), start, end, null)).thenReturn(Collections.emptyList());
				
				Menu menu = new Menu();
				int menuId = 4;
				menu.setId(menuId);
				menu.setBaseDurationMinutes(60);
				when(stylistMenuRepository.findAll(new Id<Stylist>(stylistId))).thenReturn(List.of(menu));

				List<TimeRange> result = stylistMenuService.getAvalilableEmptySlot(
						new Id<Stylist>(stylistId), targetDate, new Id<Menu>(menuId), null);
				
				assertThat(result.size()).isEqualTo(9);
				assertThat(result)
				    .extracting(TimeRange::getStartTime, TimeRange::getEndTime)
				    .containsExactly(
				    		tuple(LocalTime.of(10, 0), LocalTime.of(11, 0)),
				    		tuple(LocalTime.of(10, 30), LocalTime.of(11, 30)),
				    		tuple(LocalTime.of(11, 0), LocalTime.of(12, 0)),
				    		tuple(LocalTime.of(11, 30), LocalTime.of(12, 30)),
				    		tuple(LocalTime.of(12, 0), LocalTime.of(13, 0)),
				    		tuple(LocalTime.of(12, 30), LocalTime.of(13, 30)),
				    		tuple(LocalTime.of(13, 0), LocalTime.of(14, 0)),
				    		tuple(LocalTime.of(13, 30), LocalTime.of(14, 30)),
				    		tuple(LocalTime.of(14, 0), LocalTime.of(15, 0)));
			}
			
			@Test
			void 空き時間が施術時間より短い場合() {
				long stylistId = 1L;
				List<BusinessHour> businessHours = generateBusinessHours();
				when(businessHourRepository.findById(new Id<Stylist>(stylistId))).thenReturn(businessHours);
				
				Reservation reservation = new Reservation();
				reservation.setStartTime(LocalDateTime.of(2026, 10, 13, 10, 0));
				reservation.setEndTime(LocalDateTime.of(2026, 10, 13, 10, 30));
				LocalDateTime targetDate = LocalDateTime.of(2026, 10, 13, 10, 0, 0);
				var start = targetDate.toLocalDate().atTime(LocalTime.MIN);
				var end = targetDate.toLocalDate().atTime(LocalTime.MAX);
				when(reservationRepository.findByStylistId(new Id<Stylist>(stylistId), start, end, null)).thenReturn(List.of(reservation));
				
				Menu menu = new Menu();
				int menuId = 4;
				menu.setId(menuId);
				menu.setBaseDurationMinutes(60);
				when(stylistMenuRepository.findAll(new Id<Stylist>(stylistId))).thenReturn(List.of(menu));

				List<TimeRange> result = stylistMenuService.getAvalilableEmptySlot(
						new Id<Stylist>(stylistId), targetDate, new Id<Menu>(menuId), null);
				
				assertThat(result).isEmpty();
			}
			@Test
			void 空き時間が既存予約の前後に分割される場合() {
				
				long stylistId = 1L;
				List<BusinessHour> businessHours = generateBusinessHours();
				when(businessHourRepository.findById(new Id<Stylist>(stylistId))).thenReturn(businessHours);
				
				Reservation reservation = new Reservation();
				reservation.setStartTime(LocalDateTime.of(2026, 10, 14, 12, 0));
				reservation.setEndTime(LocalDateTime.of(2026, 10, 14, 13, 0));
				LocalDateTime targetDate = LocalDateTime.of(2026, 10, 14, 10, 0, 0);
				var start = targetDate.toLocalDate().atTime(LocalTime.MIN);
				var end = targetDate.toLocalDate().atTime(LocalTime.MAX);
				when(reservationRepository.findByStylistId(new Id<Stylist>(stylistId), start, end, null)).thenReturn(List.of(reservation));
				
				Menu menu = new Menu();
				int menuId = 4;
				menu.setId(menuId);
				menu.setBaseDurationMinutes(90);
				when(stylistMenuRepository.findAll(new Id<Stylist>(stylistId))).thenReturn(List.of(menu));

				List<TimeRange> result = stylistMenuService.getAvalilableEmptySlot(
						new Id<Stylist>(stylistId), targetDate, new Id<Menu>(menuId), null);
				
				assertThat(result.size()).isEqualTo(4);
				assertThat(result)
				    .extracting(TimeRange::getStartTime, TimeRange::getEndTime)
				    .containsExactly(tuple(LocalTime.of(10, 0), LocalTime.of(11, 30)),
				    		         tuple(LocalTime.of(10, 30), LocalTime.of(12, 0)),
				    		         tuple(LocalTime.of(13, 0), LocalTime.of(14, 30)),
				    		         tuple(LocalTime.of(13, 30), LocalTime.of(15, 0))
				    		         );
			}
			
			@Test
			void 営業時間全体が予約で埋まっている場合() {
				long stylistId = 1L;
				List<BusinessHour> businessHours = generateBusinessHours();
				when(businessHourRepository.findById(new Id<Stylist>(stylistId))).thenReturn(businessHours);
				
				Reservation reservation = new Reservation();
				reservation.setStartTime(LocalDateTime.of(2026, 10, 15, 10, 0));
				reservation.setEndTime(LocalDateTime.of(2026, 10, 15, 11, 0));
				LocalDateTime targetDate = LocalDateTime.of(2026, 10, 15, 10, 0, 0);
				var start = targetDate.toLocalDate().atTime(LocalTime.MIN);
				var end = targetDate.toLocalDate().atTime(LocalTime.MAX);
				when(reservationRepository.findByStylistId(new Id<Stylist>(stylistId), start, end, null)).thenReturn(List.of(reservation));
				
				Menu menu = new Menu();
				int menuId = 4;
				menu.setId(menuId);
				menu.setBaseDurationMinutes(60);
				when(stylistMenuRepository.findAll(new Id<Stylist>(stylistId))).thenReturn(List.of(menu));

				List<TimeRange> result = stylistMenuService.getAvalilableEmptySlot(
						new Id<Stylist>(stylistId), targetDate, new Id<Menu>(menuId), null);
				
				assertThat(result).isEmpty();
			}
		}
	}
}
