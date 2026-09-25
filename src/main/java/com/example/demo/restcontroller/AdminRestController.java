package com.example.demo.restcontroller;

import java.util.List;

import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.BusinessHours;
import com.example.demo.domain.dto.RegisterMenuForm;
import com.example.demo.domain.dto.Response;
import com.example.demo.domain.dto.UserForm;
import com.example.demo.domain.entity.Menu;
import com.example.demo.service.BusinessHourRegisterService;
import com.example.demo.service.MenuRegisterService;
import com.example.demo.service.StylistMenuService;
import com.example.demo.service.UserService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/admin")

public class AdminRestController {

	UserService userService;
	BusinessHourRegisterService businessHourRegisterService;
	MenuRegisterService menuRegisterService;
	StylistMenuService stylistMenuService;
	
	@PostMapping("/stylist")
	public void registerStylist(
			@RequestBody UserForm user) {
		userService.registerUser(user);
		
	}
	
	@PutMapping("/stylists/{id}/business-hours")
	public List<BusinessHour> registerBusinessHour(
			@PathVariable(name="id") long stylistId,
			@RequestBody BusinessHours businessHours) {
		log.info("営業時間の登録開始");
		return businessHourRegisterService.register(stylistId, businessHours);
	}
	@PostMapping("/menus")
	public Response<Menu> registerMenu(@RequestBody RegisterMenuForm menu) {
		return menuRegisterService.registerMenu(menu);
	}
	
	@PutMapping("/stylists/{id}/menus/{menuId}")
	public void assignMenuToStylist(@PathVariable long id,
			@PathVariable long menuId) {
		
		stylistMenuService.assignMenu(id, menuId);
		
		
	}
}
