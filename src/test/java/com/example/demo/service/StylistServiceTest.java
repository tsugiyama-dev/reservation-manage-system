package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
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
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.impl.CancelState;
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
