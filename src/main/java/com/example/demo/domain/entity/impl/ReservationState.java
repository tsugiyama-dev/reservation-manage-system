package com.example.demo.domain.entity.impl;

import com.example.demo.domain.Status;
import com.example.demo.domain.entity.Reservation;

public sealed interface ReservationState 
           permits Confirm, Reject, Cancel, Change, Pending {
	
	default void confirm() {
		throw new IllegalStateException("PENDINGのみ変更可能です");
	};
	default void reject() {
		throw new IllegalStateException("PENDINGのみ変更可能です");
	};
	default void cancel() {
		throw new IllegalStateException("CONFIRMのみ変更可能です");
	};
	default void change() {
		throw new IllegalStateException("PENDINGのみ変更可能です");
	};
	default void pending() {
		throw new IllegalStateException("不正な状態遷移です");
		
	}
	void set(Reservation reservation);
	Status getKey();
	
}
