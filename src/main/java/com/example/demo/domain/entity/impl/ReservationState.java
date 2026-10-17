package com.example.demo.domain.entity.impl;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

public sealed interface ReservationState 
           permits ConfirmState, RejectState, CancelState, PendingState {
	
	default void confirm(Reservation reservation) {
		throw new IllegalStateException("PENDINGのみ変更可能です");
	};
	default void reject(Reservation reservation) {
		throw new IllegalStateException("PENDINGのみ変更可能です");
	};
	default void cancel(Reservation reservation) {
		throw new IllegalStateException("CONFIRMのみ変更可能です");
	};
	default void pending(Reservation reservation) {
		throw new IllegalStateException("不正な状態遷移です");
		
	}
	Status getKey();
	
}
