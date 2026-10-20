package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.Clock;
import java.time.LocalDateTime;
import java.time.ZoneId;
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
import org.mockito.InOrder;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.impl.CancelState;
import com.example.demo.domain.entity.impl.CompleteState;
import com.example.demo.domain.entity.impl.ConfirmState;
import com.example.demo.domain.entity.impl.PendingState;
import com.example.demo.domain.entity.impl.RejectState;
import com.example.demo.domain.entity.impl.ReservationState;
import com.example.demo.domain.entity.impl.ReservationStatusResolver;
import com.example.demo.repository.ReservationRepository;


@ExtendWith(MockitoExtension.class)
class StylistServiceTest {

	@Mock
	ReservationStatusResolver resolver;
	@Mock
	AuthenticationService authenticationService;
	@Mock
	 ReservationRepository reservationRepository;
	@Mock
	ApplicationEventPublisher eventPublisher;
	
	LocalDateTime now = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
	ZoneId zone = ZoneId.of("Asia/Tokyo");
	@Spy
	Clock clock = Clock.fixed(now.atZone(zone).toInstant(), zone);

	@InjectMocks
	StylistService stylistService;
	
	@Nested
	@DisplayName("予約の確定処理")
	class 予約の確定処理 {
		
		@Nested
		@DisplayName("正常系")
		class 正常系 {
			@Test
			 void 正常に確定処理される() {
				
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				ReservationState pendingState = new PendingState();
				when(resolver.getInstance(reservation.getStatus())).thenReturn(pendingState);
				LoginUser stylist = new LoginUser(3L, "stylist@example.com");
				
				stylistService.confirm(reservationId, stylist);
				
				InOrder order = inOrder(reservationRepository, eventPublisher);
				order.verify(reservationRepository).update(reservation);
				order.verify(eventPublisher).publishEvent(reservation);
				assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED);
			}
		}
		@Nested
		@DisplayName("異常系")
		class 異常系 {
			@Test
			void 間違った予約IDを指定してNoSuchElementExceptionを投げる() {
				long reservationId = 2L;
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.empty());
				
				long stylistId = 3L;
				LoginUser stylist = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.confirm(reservationId, stylist))
				 .isInstanceOf(NoSuchElementException.class);
				
				verifyNoInteractions(eventPublisher);
				verify(reservationRepository, never()).update(any());
			}
			@Test
			void 担当者以外が操作してAccessDeniedExceptionを投げる() {
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				long invalidStylistId = 999L;
				LoginUser invalidStylist = new LoginUser(invalidStylistId, "stylist@example.com");
				doThrow(AccessDeniedException.class).when(authenticationService).identify(invalidStylist, reservation.getStylistId());
				
				assertThatThrownBy(()-> stylistService.confirm(reservationId, invalidStylist))
				.isInstanceOf(AccessDeniedException.class);
				
				verifyNoInteractions(eventPublisher);
				verify(reservationRepository, never()).update(any());
				assertThat(reservation.getStatus()).isEqualTo(Status.PENDING);
			}
			@ParameterizedTest
			@MethodSource("不正なステータスリスト")
			void 不正なステータスでIllegalStateExceptionを投げる(Status status, ReservationState state) {
				long reservationId = 1L;
				Reservation reservation = createReservation(status);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				when(resolver.getInstance(status)).thenReturn(state);

				long stylistId = 3L;
				LoginUser stylist = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.confirm(reservationId, stylist))
				    .isInstanceOf(IllegalStateException.class);
				
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
				assertThat(reservation.getStatus()).isEqualTo(status);
			}
			
			static Stream<Arguments> 不正なステータスリスト() {
				return Stream.of(Arguments.of(Status.CANCELLED, new CancelState()),
						         Arguments.of(Status.REJECTED, new RejectState()),
						         Arguments.of(Status.COMPLETED, new CompleteState()),
						         Arguments.of(Status.CONFIRMED, new ConfirmState()));
			}
			
			@Test
			void 保存に失敗した場合通知を発行しない() {
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				ReservationState pendingState = new PendingState();
				when(resolver.getInstance(reservation.getStatus())).thenReturn(pendingState);
				LoginUser stylist = new LoginUser(3L, "stylist@example.com");
				
				var failure = new DataAccessResourceFailureException("DB接続失敗");
				doThrow(failure).when(reservationRepository).update(reservation);
				
				assertThatThrownBy(() -> stylistService.confirm(reservationId, stylist))
				    .isSameAs(failure);
				
				verifyNoInteractions(eventPublisher);
			}
		}
	}
	@Nested
	@DisplayName("予約の拒否処理")
	class 予約の拒否処理 {
		
		@Nested
		@DisplayName("正常系")
		class 正常系 {
			@Test
			void testReject() {
				long reservationId = 1L;
				long stylistId = 3L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				ReservationState pendingState = new PendingState();
				when(resolver.getInstance(reservation.getStatus())).thenReturn(pendingState);
				LoginUser stylist = new LoginUser(stylistId, "stylist@example.com");
				
				stylistService.reject(reservationId, stylist);
				
				InOrder order = inOrder(reservationRepository, eventPublisher);
				order.verify(reservationRepository).update(reservation);
				order.verify(eventPublisher).publishEvent(reservation);
				assertThat(reservation.getStatus()).isEqualTo(Status.REJECTED);
			}
		}
		@Nested
		@DisplayName("異常系")
		class 異常系 {
			@Test
			void 間違った予約IDを指定してNoSuchElementExceptionを投げる() {
				long reservationId = 2L;
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.empty());
				
				long stylistId = 3L;
				LoginUser stylist = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.reject(reservationId, stylist))
				 .isInstanceOf(NoSuchElementException.class);
				
				verifyNoInteractions(eventPublisher);
				verify(reservationRepository, never()).update(any());
			}
			@Test
			void 担当者以外が操作してAccessDeniedExceptionを投げる() {
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				long invalidStylistId = 999L;
				LoginUser invalidStylist = new LoginUser(invalidStylistId, "stylist@example.com");
				doThrow(AccessDeniedException.class).when(authenticationService).identify(invalidStylist, reservation.getStylistId());
				
				assertThatThrownBy(()-> stylistService.reject(reservationId, invalidStylist))
				.isInstanceOf(AccessDeniedException.class);
				
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
				assertThat(reservation.getStatus()).isEqualTo(Status.PENDING);
			}
			@ParameterizedTest
			@MethodSource("不正なステータスリスト")
			void 不正なステータスでIllegalStateExceptionを投げる(Status status, ReservationState state) {
				long reservationId = 1L;
				Reservation reservation = createReservation(status);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				when(resolver.getInstance(status)).thenReturn(state);

				long stylistId = 3L;
				LoginUser stylist = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.reject(reservationId, stylist))
				    .isInstanceOf(IllegalStateException.class);
				
				verify(reservationRepository, never()).update(any());
				verify(eventPublisher, never()).publishEvent(any());
				verifyNoInteractions(eventPublisher);
				assertThat(reservation.getStatus()).isEqualTo(status);
			}
			
			static Stream<Arguments> 不正なステータスリスト() {
				return Stream.of(Arguments.of(Status.CANCELLED, new CancelState()),
						         Arguments.of(Status.REJECTED, new RejectState()),
						         Arguments.of(Status.COMPLETED, new CompleteState()),
						         Arguments.of(Status.CONFIRMED, new ConfirmState()));
			}
			@Test
			void 保存に失敗した場合通知を発行しない() {
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.PENDING);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				ReservationState pendingState = new PendingState();
				when(resolver.getInstance(reservation.getStatus())).thenReturn(pendingState);
				LoginUser stylist = new LoginUser(3L, "stylist@example.com");
				
				var failure = new DataAccessResourceFailureException("DB接続失敗");
				doThrow(failure).when(reservationRepository).update(reservation);
				
				assertThatThrownBy(() -> stylistService.reject(reservationId, stylist))
				    .isSameAs(failure);
				
				verifyNoInteractions(eventPublisher);
			}
		}	
	}
	@Nested()
	@DisplayName("予約の完了処理")
	class 予約の完了処理 {
		
		@Nested
		@DisplayName("正常系")
		class 正常系 {
			
			@Test
			void 担当者が終了日時を過ぎている場合COMPLETEに更新できる() {
				// 準備
				long reservationId = 1L;
				long stylistId = 3L;
				Reservation reservation = createReservation(Status.CONFIRMED);
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				
				when(reservationRepository.findById(new Id<Reservation>(reservationId)))
				.thenReturn(Optional.of(reservation));
				when(resolver.getInstance(reservation.getStatus())).thenReturn(new ConfirmState());
				// 実行
				stylistService.complete(reservationId, user);
				
				// verify
				assertThat(reservation.getStatus()).isEqualTo(Status.COMPLETED);
				verify(reservationRepository, times(1)).update(reservation);
				verify(eventPublisher, times(1)).publishEvent(reservation);

			}
			@Test
			void 担当者が終了日時ちょうどの場合COMPLETEに更新できる() {
				// 準備
				long reservationId = 1L;
				long stylistId = 3L;
				Reservation reservation = createReservation(Status.CONFIRMED);
				reservation.setEndTime(LocalDateTime.of(2026, 1, 1, 12, 0, 0)); // 終了日時を現在日時に差し替え
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				
				when(reservationRepository.findById(new Id<Reservation>(reservationId)))
				.thenReturn(Optional.of(reservation));
				when(resolver.getInstance(reservation.getStatus())).thenReturn(new ConfirmState());
				// 実行
				stylistService.complete(reservationId, user);
				
				// verify
				LocalDateTime expectedStartTime = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
				LocalDateTime expectedEndTime = LocalDateTime.of(2026, 1, 1, 12, 0, 0);
				
				assertThat(reservation.getStatus()).isEqualTo(Status.COMPLETED);
				assertThat(reservation.getId()).isEqualTo(reservationId);
				assertThat(reservation.getStylistId()).isEqualTo(stylistId);
				assertThat(reservation.getCustomerId()).isEqualTo(2L);
				assertThat(reservation.getMenuId()).isEqualTo(4L);
				assertThat(reservation.getStartTime()).isEqualTo(expectedStartTime);
				assertThat(reservation.getEndTime()).isEqualTo(expectedEndTime);
				verify(authenticationService, times(1)).identify(user, reservation.getStylistId());
				verify(reservationRepository, times(1)).update(reservation);
				verify(eventPublisher, times(1)).publishEvent(reservation);
			}
		}
		@Nested
		@DisplayName("異常系")
		class 異常系 {
			
			@Test
			void 終了日時より前にステータスを変更するとIllegalStateExceptionが発生() {
				// 準備
				
				long reservationId = 1L;
				long stylistId = 3L;
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				Reservation reservation = createReservation(Status.CONFIRMED);
				reservation.setEndTime(LocalDateTime.of(2026, 1, 1, 12, 1, 0)); // 現在時刻より後の時刻に差し替え
				
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				// 実行
				assertThatThrownBy(() -> stylistService.complete(reservationId, user))
				 .isInstanceOf(IllegalStateException.class)
				 .hasMessage("予約時間より前にステータスを変更できません");
				// 検証
				assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED);
				verify(authenticationService).identify(user, stylistId);
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
			}
			@Test
			void 別担当者がステータスを操作をするとAccessDeniedExceptionが発生() {
				// 準備
				
				long reservationId = 1L;
				long stylistId = 2L;
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				Reservation reservation = createReservation(Status.CONFIRMED);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				doThrow(AccessDeniedException.class).when(authenticationService).identify(user, reservation.getStylistId());
				// 実行
				assertThatThrownBy(() -> stylistService.complete(reservationId, user))
				.isInstanceOf(AccessDeniedException.class);
				// 検証
				assertThat(reservation.getStatus()).isEqualTo(Status.CONFIRMED);
				verify(authenticationService).identify(user, reservation.getStylistId());
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
			}
			
			@Test
			void 指定された予約が存在しない場合NoSuchElementExceptionが発生() {
				
				// arrange
				long nonExistentId = 999L;
				when(reservationRepository.findById(new Id<Reservation>(nonExistentId))).thenReturn(Optional.<Reservation>empty());
				// 実行
				long stylistId = 3L;
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.complete(nonExistentId, user))
				.isInstanceOf(NoSuchElementException.class);
				// 検証
	
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
			}
			@ParameterizedTest
			@MethodSource("statusリスト")
			void statusがCONFIRMED以外のときにIllegalStateExceptionが発生(Status status, ReservationState state) {
				
				// arrange
				long reservationId = 1L;
				Reservation reservation = createReservation(status);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				when(resolver.getInstance(reservation.getStatus())).thenReturn(state);

				// act
				long stylistId = 3L;
				LoginUser user = new LoginUser(stylistId, "stylist@example.com");
				assertThatThrownBy(() -> stylistService.complete(reservationId, user))
				.isInstanceOf(IllegalStateException.class)
				.hasMessage("不正な状態遷移です");
				// 検証
				assertThat(reservation.getStatus()).isEqualTo(status);
				verify(reservationRepository, never()).update(any());
				verifyNoInteractions(eventPublisher);
			}
			
			private static Stream<Arguments> statusリスト() {
				return Stream.of(
						Arguments.of(Status.PENDING, new PendingState()),
						Arguments.of(Status.CANCELLED, new CancelState()),
						Arguments.of(Status.COMPLETED, new CompleteState()),
						Arguments.of(Status.REJECTED, new RejectState()));
			}
		}
	}
	private Reservation createReservation(Status status) {
		Reservation reservation = new Reservation();
		LocalDateTime start = LocalDateTime.of(2026, 1, 1, 10, 0, 0);
		reservation.setId(1L);
		reservation.setCustomerId(2L);
		reservation.setStylistId(3L);
		reservation.setMenuId(4L);
		reservation.setStartTime(start);
		reservation.setEndTime(start.plusMinutes(90));
		reservation.setStatus(status);
		return reservation;
	}
}
