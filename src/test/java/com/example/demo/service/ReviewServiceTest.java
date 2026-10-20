package com.example.demo.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

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
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.access.AccessDeniedException;

import com.example.demo.config.LoginUser;
import com.example.demo.domain.Status;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.ReviewPostForm;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.repository.ReservationRepository;
import com.example.demo.repository.ReviewRepository;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

	@Mock
	ReviewRepository reviewRepository;
	@Mock
	ReservationRepository reservationRepository;
	@Mock
	AuthenticationService authenticationService;
	
	@InjectMocks
	ReviewService reviewService;
	
	@Nested
	@DisplayName("レビューの投稿")
	class レビューの投稿 {
		
		@Nested
		@DisplayName("正常系")
		class 正常系 {
		
			@Test
			void 予約者本人が施術後にレビュー登録ができる () {
				
				// arrange
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.COMPLETED);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));

				// act
				int point = 4;
				String comment = "施術が丁寧で私に合うヘアスタイルにしていただきました";				
				ReviewPostForm form = new ReviewPostForm(point, comment);
				LoginUser user = new LoginUser(3L, "customer@example.com"); // 予約のcustomerIdと同値を設定
				reviewService.post(user, form, reservationId);
				
				// 評価
				verify(authenticationService, times(1)).identify(user, 3L);
				verify(reviewRepository, times(1)).insert(reservationId, form);
				verify(reservationRepository, never()).update(any());
				assertThat(reservation.getStatus()).isEqualTo(Status.COMPLETED);
			}
	     }
		@Nested
		@DisplayName("異常系")
		class 異常系 {
			
			@ParameterizedTest
			@MethodSource("不正なstatusリスト")
			void statusがCOMPLETED以外で投稿するとIllegalStateExceptionが発生する (
					Status status) {
				// arrange
				long reservationId = 1L;
				Reservation reservation = createReservation(status);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));
				
				// act
				int point = 4;
				String comment = "施術が丁寧で私に合うヘアスタイルにしていただきました";				
				ReviewPostForm form = new ReviewPostForm(point, comment);
				LoginUser user = new LoginUser(3L, "customer@example.com"); // 予約のcustomerIdと同値を設定
				assertThatThrownBy(() -> reviewService.post(user, form, reservationId))
				  .isInstanceOf(IllegalStateException.class)
				  .hasMessage("statusがCOMPLETEDのみレビューを投稿できます");
				
				// 評価
				verify(authenticationService, times(1)).identify(user, 3L);
				verify(reviewRepository, never()).insert(anyLong()	, any());
				verify(reservationRepository, never()).update(any());
				assertThat(reservation.getStatus()).isEqualTo(status);
				
			}
			
			static Stream<Arguments> 不正なstatusリスト () {
				return Stream.of(
						Arguments.of(Status.PENDING),
						Arguments.of(Status.CONFIRMED),
						Arguments.of(Status.REJECTED),
						Arguments.of(Status.CANCELLED));
			}
			
			@Test
			void 本人以外がレビュー投稿するとAccessDeniedExceptionが発生() {
				// arrange
				long reservationId = 1L;
				Reservation reservation = createReservation(Status.COMPLETED);
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.of(reservation));

				LoginUser user = new LoginUser(9999L, "customer@example.com"); // 予約のcustomerId異なるユーザーIDを設定
				doThrow(new AccessDeniedException("本人以外操作することはできません")).when(authenticationService).identify(user, reservation.getCustomerId());
				// act
				int point = 4;
				String comment = "施術が丁寧で私に合うヘアスタイルにしていただきました";				
				ReviewPostForm form = new ReviewPostForm(point, comment);
				assertThatThrownBy(() -> reviewService.post(user, form, reservationId))
				  .isInstanceOf(AccessDeniedException.class)
				  .hasMessage("本人以外操作することはできません");
				
				// 評価
				verify(authenticationService, times(1)).identify(user, 3L);
				verify(reviewRepository, never()).insert(anyLong(), any());
				verify(reservationRepository, never()).update(any());
			}
			@Test
			void 指定した予約が存在しないとNoSuchElementExceptionが発生() {
				// arrange
				long reservationId = 9999L;
				when(reservationRepository.findById(new Id<Reservation>(reservationId))).thenReturn(Optional.empty());
				
				LoginUser user = new LoginUser(9999L, "customer@example.com"); // 予約のcustomerId異なるユーザーIDを設定
				// act
				int point = 4;
				String comment = "施術が丁寧で私に合うヘアスタイルにしていただきました";				
				ReviewPostForm form = new ReviewPostForm(point, comment);
				assertThatThrownBy(() -> reviewService.post(user, form, reservationId))
				.isInstanceOf(NoSuchElementException.class)
				.hasMessageContaining("予約が見つかりません");
				
				// 評価
			    verify(reservationRepository).findById(new Id<Reservation>(reservationId));
				verify(reviewRepository, never()).insert(anyLong()	, any());
				verify(reservationRepository, never()).update(any());
			}
		}
     }
	private Reservation createReservation(Status status) {
		Reservation reservation = new Reservation();
		reservation.setId(1L);
		reservation.setStylistId(2L);
		reservation.setCustomerId(3L);
		reservation.setMenuId(4L);
		reservation.setStatus(status);
		//予約時間は使用しないため省略
		return reservation;
		
	}
}