package com.example.demo.event;

import java.time.format.DateTimeFormatter;
import java.util.NoSuchElementException;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.entity.Reservation;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.UserRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Component
@AllArgsConstructor
@Slf4j
public class MailNotification {

	private final JavaMailSender mailSender;
	private final UserRepository userRepository;
	
	@Async
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(Reservation reservation) {
	
		log.info("メール送信処理開始");
		User user = userRepository.findById(new Id<User>(reservation.getCustomerId())).orElseThrow(
				() -> {
					throw new NoSuchElementException("");
					});
		String status = reservation.getStatus().name();
		String startTime = reservation.getStartTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		String endTime = reservation.getEndTime().format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm"));
		SimpleMailMessage message = new SimpleMailMessage();
		String text = """
				以下の日時の予約が%sされました。
				開始時間：%s
				終了予定時刻:%s
				
				""".formatted(status.equals("CONFIRMED") ? "確定" : "キャンセル"
					        , startTime
					        , endTime);
		message.setSubject("美容室の予約について");
		message.setText(text);
		message.setFrom("sender_exclusive@example.com");
		message.setTo(user.getEmail());
		mailSender.send(message);
		log.info("メール送信処理終了");
	}
}
