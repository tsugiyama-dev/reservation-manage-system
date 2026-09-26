package com.example.demo.restcontroller;

import java.util.List;

import jakarta.validation.Valid;

import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
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
import com.example.demo.domain.dto.StylistOriginalMenu;
import com.example.demo.domain.dto.UserForm;
import com.example.demo.domain.entity.Menu;
import com.example.demo.service.AuthenticationService;
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

public class AdminController {

	UserService userService;
	BusinessHourRegisterService businessHourRegisterService;
	MenuRegisterService menuRegisterService;
	StylistMenuService stylistMenuService;
	
	@PostMapping("/stylist")
	public void registerStylist(
			@RequestBody UserForm user,
			Authentication auth) {
		
		checkAuthority(auth);
		userService.registerUser(user);
		
	}
	
	@PutMapping("/stylists/{id}/business-hours")
	public List<BusinessHour> registerBusinessHour(
			@PathVariable(name="id") long stylistId,
			@RequestBody BusinessHours businessHours,
			Authentication auth) {
		
		checkAuthority(auth);
		log.info("営業時間の登録開始");
		return businessHourRegisterService.register(stylistId, businessHours);
	}
	
	@PostMapping("/menus")
	public Response<Menu> registerMenu(@RequestBody RegisterMenuForm menu,
			Authentication auth) {
		
		checkAuthority(auth);
		return menuRegisterService.registerMenu(menu);
	}
	@DeleteMapping("/stylist/{id}")
	public void deleteStylist(@PathVariable long id,
			Authentication auth) {
		checkAuthority(auth);
		userService.delete(id);
		
	}
	
	@PutMapping("/stylists/{id}/menus/{menuId}")
	public void assignMenu(@PathVariable long id,
			@PathVariable long menuId,
			@RequestBody @Valid StylistOriginalMenu original,
			Authentication auth) {
		
		checkAuthority(auth);
		stylistMenuService.assignMenu(id, menuId, original);
	}
	
	
	private void checkAuthority(Authentication auth) {
		if(!AuthenticationService.isAdmin(auth)) {
			throw new AccessDeniedException("管理者に依頼してください");
		};
	}
}
