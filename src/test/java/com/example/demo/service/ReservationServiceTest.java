package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Stream;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.TimeRange;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReservationRequest;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.impl.CancelState;
import com.example.demo.domain.entity.impl.CompleteState;
import com.example.demo.domain.entity.impl.ConfirmState;
import com.example.demo.domain.entity.impl.PendingState;
import com.example.demo.domain.entity.impl.RejectState;
import com.example.demo.domain.entity.impl.ReservationState;
import com.example.demo.domain.entity.impl.ReservationStatusResolver;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;

@ExtendWith(MockitoExtension.class)
class ReservationServiceTest {

	@Mock StylistRepository stylistRepository;
	@Mock ReservationRepository reservationRepository;
	@Mock StylistMenuRepository stylistMenuRepository;
	@Mock StylistMenuService stylistMenuService;
	@Mock ReservationStatusResolver reservationStatusResolver;
	@Mock AuthenticationService authenticationService;
	@Mock PendingState pendingStatus;

	@InjectMocks ReservationService reservationService;
	@Captor ArgumentCaptor<Reservation> argumentCaptor;

	@Nested
	@DisplayName("予約")
	class 予約リクエスト{
		@Test
		void 正常に登録される() {
			LocalDateTime date = LocalDateTime.of(2024, 6, 1, 10, 0);
			ReservationRequest<Stylist, Menu> req = new ReservationRequest<>(new Id<>(1), date, new Id<>(2));
			LoginUser user = new LoginUser(3, "test@example.com");
			
			Menu menu1 = new Menu(1, "cut", 30, 5000);
			Menu menu2 = new Menu(2, "color", 60, 5000);
			
			TimeRange $10_00_11_00 = new TimeRange(date.toLocalTime(), date.toLocalTime().plusMinutes(60));
			
			when(stylistMenuRepository.findAll(req.stylistId())).thenReturn(List.of(menu1, menu2));
			when(stylistMenuService.getAvalilableEmptySlot(req.stylistId(), req.date(), req.menuId(), null))
			.thenReturn(List.of($10_00_11_00));
			
			reservationService.reserve(req, user);
			
			verify(stylistRepository, times(1)).lockStylist(req.stylistId());
			verify(reservationRepository).insert(argumentCaptor.capture());
			assertThat(argumentCaptor.getValue().getStatus()).isEqualTo(Status.PENDING);
			assertThat(argumentCaptor.getValue().getCustomerId()).isEqualTo(3);
			assertThat(argumentCaptor.getValue().getStylistId()).isEqualTo(req.stylistId().id());
			assertThat(argumentCaptor.getValue().getMenuId()).isEqualTo(req.menuId().id());
			assertThat(argumentCaptor.getValue().getStartTime()).isEqualTo(date);
			assertThat(argumentCaptor.getValue().getEndTime()).isEqualTo(date.plusMinutes(60));
		}
		
		@Test
		void 予約可能枠がないので登録されない() {
			LocalDateTime date = LocalDateTime.of(2024, 6, 1, 10, 0); 
			ReservationRequest<Stylist, Menu> req = new ReservationRequest<>(new Id<>(1), date, new Id<>(2));
			LoginUser user = new LoginUser(1, "test@example.com");
			
			Menu menu1 = new Menu(1, "cut", 60, 5000); // 2024-06-01 10:00 ~ 10:30
			Menu menu2 = new Menu(2, "color", 90, 5000); // 2024-06-01 10:00 ~ 11:00
			
			when(stylistMenuRepository.findAll(req.stylistId())).thenReturn(List.of(menu1, menu2));
			when(stylistMenuService.getAvalilableEmptySlot(req.stylistId(), req.date(), req.menuId(), null))
			.thenReturn(List.of());
			
			assertThatThrownBy(() -> reservationService.reserve(req, user))
			.isInstanceOf(IllegalStateException.class)
			.hasMessageContaining("予約可能な枠がありません");
			
			verify(reservationRepository, never()).insert(any());
		}
		@Test
		void スタイリストの対応メニューにないリクエスト() {
			LocalDateTime date = LocalDateTime.of(2024, 6, 1, 10, 0); 
			ReservationRequest<Stylist, Menu> req = new ReservationRequest<>(new Id<>(1), date, new Id<>(9999));
			LoginUser user = new LoginUser(1, "test@example.com");
			Menu menu1 = new Menu(1, "cut", 60, 5000); 
			
			when(stylistMenuRepository.findAll(req.stylistId())).thenReturn(List.of(menu1));
			
			assertThatThrownBy(() -> reservationService.reserve(req, user))
			.isInstanceOf(NoSuchElementException.class)
			.hasMessageContaining("存在しないメニューＩＤです");
			
			verify(reservationRepository, never()).insert(any());
		}
	}
	
	@Nested
	@DisplayName("キャンセル")
	class 予約キャンセル {
		
		@MethodSource("ステータスリスト")
		@ParameterizedTest
		void  正常にキャンセルできる(Status status, ReservationState state) {
			long requestId = 111L;
			LoginUser user = new LoginUser(1L, "test@example.com");
			Reservation reservation = new Reservation();
			reservation.setCustomerId(1L);
			reservation.setStatus(status);
			
			when(reservationRepository.findById(new Id<Reservation>(requestId))).thenReturn(Optional.of(reservation));
			when(reservationStatusResolver.getInstance(status)).thenReturn(state);
			reservationService.cancel(requestId, user);
			
			verify(reservationRepository, times(1)).update(reservation);
			assertThat(reservation.getStatus()).isEqualTo(Status.CANCELLED);
			
		}
		static Stream<Arguments> ステータスリスト() {
			return Stream.of(
					Arguments.of(Status.PENDING, new PendingState()),
					Arguments.of(Status.CONFIRMED, new ConfirmState()));
		}
		@Test
		void 予約が存在しない場合NoSuchElementExceptionを返す() {
			long requestId = 999L;
			LoginUser user = new LoginUser(1L, "test@example.com");
			when(reservationRepository.findById(new Id<Reservation>(requestId))).thenReturn(Optional.empty());
					
			assertThatThrownBy(() -> reservationService.cancel(requestId, user)).isInstanceOf(NoSuchElementException.class);
		    
			verify(reservationRepository, never()).update(any(Reservation.class));
		}
		@Test
		void 予約者本人ではない場合AccessDeniedExceptionを返す() {
			long requestId = 1L;
			LoginUser user = new LoginUser(999L, "bad_user@example.com");
			
			when(reservationRepository.findById(new Id<Reservation>(requestId)))
			                          .thenReturn(Optional.of(new Reservation()));
			doThrow(new AccessDeniedException("本人以外操作することはできません")).when(authenticationService).identify(any(LoginUser.class), anyLong());
			
			assertThatThrownBy(() -> reservationService.cancel(requestId, user))
			                         .isInstanceOf(AccessDeniedException.class);
		    
			verify(reservationRepository, never()).update(any());
		}
		@ParameterizedTest
		@MethodSource("cancelパラメータ")
		public void 状態がPENDINGとCONFIRMED以外はIllegalStateExceptionを投げる(Status status, ReservationState state) {
		
			Reservation reservation = new Reservation();
			 reservation.setStatus(status);
			 reservation.setCustomerId(1L);
			 reservation.setId(111L);
			 
			 when(reservationRepository.findById(new Id<Reservation>(111L))).thenReturn(Optional.of(reservation));
			 when(reservationStatusResolver.getInstance(reservation.getStatus())).thenReturn(state);
			 
			 LoginUser user = new LoginUser(1L, "test@example.com");
			 assertThatThrownBy(() -> reservationService.cancel(111L, user))
			 .as("cancelはPedingとConfirmのみ")
			 .isInstanceOf(IllegalStateException.class);
			 verify(reservationRepository, never()).update(any());
			 
		}
		
		static Stream<Arguments> cancelパラメータ() {
			return Stream.of(
					Arguments.of(Status.CANCELLED, new CancelState()),
					Arguments.of(Status.COMPLETED, new CompleteState()),
					Arguments.of(Status.REJECTED, new RejectState()));
		}
	}
	@Nested
	@DisplayName("変更")
	class 変更 {
		
		@Nested
		class 正常系 {
			@MethodSource("changeパラメータ")
			@ParameterizedTest
			void 予約の変更ができる(Status status, ReservationState state) {
				long reservationId = 1L;
				Reservation reservation = create(reservationId, status);
				
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				Menu menu = new Menu();
				menu.setBaseDurationMinutes(60);
				menu.setId(50);
				when(stylistMenuRepository.findAll(new Id<Stylist>(reservation.getStylistId())))
				.thenReturn(List.of(menu));
				
				ReservationRequest<Stylist, Menu> request = new ReservationRequest<>(LocalDateTime.of(2026, 10, 8, 13, 0, 0));
				when(stylistMenuService.getAvalilableEmptySlot(new Id<Stylist>(reservation.getStylistId()),
						request.date(),
						new Id<Menu>(reservation.getMenuId()),
						reservationId))
				.thenReturn(List.of(new TimeRange(LocalTime.of(13, 0, 0), LocalTime.of(14, 0, 0))));
				
				when(reservationStatusResolver.getInstance(reservation.getStatus())).thenReturn(state);
				
				LoginUser user = new LoginUser(30L, "test@example.com");
				reservationService.change(user, reservationId, request);
				
				verify(reservationRepository, times(1)).update(reservation);
				verify(stylistRepository, times(1)).lockStylist(new Id<Stylist>(40L));
				verify(reservationRepository, never()).insert(any());
				assertThat(reservation.getStatus()).isEqualTo(Status.PENDING);
				assertThat(reservation.getStartTime()).isEqualTo(request.date());
				assertThat(reservation.getEndTime()).isEqualTo(request.date().plusMinutes(menu.getBaseDurationMinutes()));
				assertThat(reservation.getId()).isEqualTo(reservationId);
				assertThat(reservation.getCustomerId()).isEqualTo(30L);
				assertThat(reservation.getStylistId()).isEqualTo(40L);
				assertThat(reservation.getMenuId()).isEqualTo(50l);
				
			}
			static Stream<Arguments> changeパラメータ() {
				return Stream.of(
						Arguments.of(Status.PENDING, new  PendingState()),
						Arguments.of(Status.CONFIRMED, new ConfirmState()));
			}
		}
		@Nested
		@DisplayName("異常系")
		class 異常系 {
			
			@Test
			void 存在しない予約を更新しようとしてNoSuchElementExceptionを投げる() {
				long notExistId = 999L;
				when(reservationRepository.findById(new Id<Reservation>(notExistId))).thenReturn(Optional.empty());
				
				LoginUser user = new LoginUser(1l, "test@example.com");
				Id<Stylist> stylistId = new Id<>(30l);
				Id<Menu> menuId = new Id<>(40l);
				LocalDateTime date = LocalDateTime.of(2026, 10, 9, 12, 0, 0);
				ReservationRequest<Stylist, Menu> req = new ReservationRequest<Stylist, Menu>(stylistId, date, menuId);
				assertThatThrownBy(() -> reservationService.change(user, notExistId, req))
				.as("予約が存在しない")
				.isInstanceOf(NoSuchElementException.class);
            	verify(reservationRepository, never()).update(any(Reservation.class));
				verify(reservationRepository, never()).insert(any());
			}
			
			@Test
			void 空き枠なしで例外をスローする() {

				long reservationId = 111L;
				Status pending = Status.PENDING;
				Reservation reservation = create(reservationId, pending);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				
				LoginUser user = new LoginUser(30l, "test@example.com");
				Id<Stylist> stylistId = new Id<>(reservation.getStylistId());
				Id<Menu> menuId = new Id<>(reservation.getMenuId());
				LocalDateTime date = LocalDateTime.of(2026, 10, 9, 12, 0, 0);
				ReservationRequest<Stylist, Menu> req = 
						new ReservationRequest<Stylist, Menu>(date);
				when(stylistMenuService.getAvalilableEmptySlot(stylistId, date, menuId, reservationId)).thenReturn(List.<TimeRange>of());

				Menu menu = new Menu();
				menu.setId(50);
				menu.setBaseDurationMinutes(60);
				when(stylistMenuRepository.findAll(stylistId)).thenReturn(List.of(menu));
				assertThatThrownBy(() -> reservationService.change(user, reservationId, req))
				.isInstanceOf(IllegalStateException.class);
				
            	verify(reservationRepository, never()).update(any(Reservation.class));
				verify(reservationRepository, never()).insert(any());
			}
		}
		
		@Test
		void 本人確認失敗() {
			long reservationId = 111L;
			Reservation reservation = create(reservationId, Status.PENDING);
			when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
			
			LoginUser badUser = new LoginUser(20L, "BadUser@example.com");
			doThrow(new AccessDeniedException("本人以外、予約を変更することはできません")).when(authenticationService).identify(badUser, reservation.getCustomerId());
			
			ReservationRequest<Stylist, Menu> req = new ReservationRequest<>(LocalDateTime.now());
			assertThatThrownBy(() -> reservationService.change(badUser, reservationId, req))
			.isInstanceOf(AccessDeniedException.class)
			.hasMessageContaining("本人以外");
			
			verify(reservationRepository, never()).update(any());
			
		}
		@Test
		void 予約したメニューがDBにない() {
			
			long reservationId = 111L;
			Reservation reservation = create(reservationId, Status.PENDING);
			when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));

			Menu nonexistent = new Menu();
			nonexistent.setId(999);
			when(stylistMenuRepository.findAll(new Id<Stylist>(reservation.getStylistId()))).thenReturn(List.of(nonexistent));
			
			LoginUser user = new LoginUser(30L, "user@example.com");
			assertThatThrownBy(() -> reservationService.change(user, reservationId, new ReservationRequest<Stylist, Menu>(LocalDateTime.now())))
			.isInstanceOf(NoSuchElementException.class);
			
			verify(reservationRepository, never()).update(any());
	
		}
	
		
		private Reservation create(Long reservationId, Status status) {
			Reservation reservation =  new Reservation();;
			reservation.setId(reservationId);
			reservation.setCustomerId(30L);
			reservation.setStylistId(40L);
			reservation.setMenuId(50L);
			reservation.setStatus(status);
			reservation.setStartTime(LocalDateTime.of(2026, 10, 7, 12, 0, 0));
			reservation.setEndTime(LocalDateTime.of(2026, 10, 7, 13, 0, 0));
 	        return reservation;	
 		}
		@MethodSource("changeできない状態パラメータ")
		@ParameterizedTest
		void 予約変更ができないステータスのテスト(Status status, ReservationState state) {
			
			long reservationId = 1L;
			Reservation reservation = create(reservationId, status);
			when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
			when(reservationStatusResolver.getInstance(reservation.getStatus())).thenReturn(state);
			
			Menu menu = new Menu();
			menu.setBaseDurationMinutes(60);
			menu.setId(50);
			when(stylistMenuRepository.findAll(new Id<Stylist>(reservation.getStylistId())))
			.thenReturn(List.of(menu));
			
			ReservationRequest<Stylist, Menu> request = new ReservationRequest<>(LocalDateTime.of(2026, 10, 8, 13, 0, 0));
			when(stylistMenuService.getAvalilableEmptySlot(new Id<Stylist>(reservation.getStylistId()),
					request.date(),
					new Id<Menu>(reservation.getMenuId()),
					reservationId))
			.thenReturn(List.of(new TimeRange(LocalTime.of(13, 0, 0), LocalTime.of(14, 0, 0))));
			
			LocalDateTime beforeStart = reservation.getStartTime();
			LocalDateTime beforeEnd = reservation.getEndTime();

			LoginUser user = new LoginUser(30L, "test@example.com");
			assertThatThrownBy(() -> reservationService.change(user, reservationId, request))
			.isInstanceOf(IllegalStateException.class);
			
			verify(stylistRepository, times(1)).lockStylist(new Id<Stylist>(40L));
			verify(reservationRepository, never()).update(any());
			verify(reservationRepository, never()).insert(any());
			assertThat(reservation.getStatus()).isEqualTo(status);
			assertThat(reservation.getStartTime()).isEqualTo(beforeStart);
			assertThat(reservation.getEndTime()).isEqualTo(beforeEnd);
			assertThat(reservation.getId()).isEqualTo(reservationId);
			assertThat(reservation.getCustomerId()).isEqualTo(30L);
			assertThat(reservation.getStylistId()).isEqualTo(40L);
			assertThat(reservation.getMenuId()).isEqualTo(50l);
			
		}
		static Stream<Arguments> changeできない状態パラメータ() {
			return Stream.of(
					Arguments.of(Status.CANCELLED, new  CancelState()),
					Arguments.of(Status.COMPLETED, new  CompleteState()),
					Arguments.of(Status.REJECTED, new RejectState()));
		}
	}
	
}
