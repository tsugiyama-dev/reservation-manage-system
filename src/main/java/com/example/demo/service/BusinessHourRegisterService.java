package com.example.demo.service;

import java.util.List;

import org.springframework.stereotype.Service;

import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.BusinessHours;
import com.example.demo.domain.dto.Id;
import com.example.demo.repository.BusinessHourRepository;

import lombok.AllArgsConstructor;

@Service
@AllArgsConstructor
public class BusinessHourRegisterService {

	BusinessHourRepository businessHourRepository;
	
	/***
	 * 営業時間を登録する処理
	 * 既に営業時間が登録されている場合は、更新ではなく削除して登録
	 * @param stylistId
	 * @param list
	 * @return List<BusinessHour>
	 */
	public List<BusinessHour> register(long stylistId, BusinessHours list) {
		
		if(businessHourRepository.findById(new Id<BusinessHour>(stylistId)) != null) {
			businessHourRepository.delete(stylistId);
		}
		for(BusinessHour e : list.businessHours()) {
			businessHourRepository.insert(stylistId, e);
		}
		return businessHourRepository.findById(new Id<BusinessHour>(stylistId));
	}
}
