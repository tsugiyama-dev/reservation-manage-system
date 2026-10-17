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
import com.example.demo.domain.dto.MenuRegisterForm;
import com.example.demo.domain.dto.OriginalMenu;
import com.example.demo.domain.dto.Response;
import com.example.demo.domain.dto.UserRegisterForm;
import com.example.demo.domain.entity.Menu;
import com.example.demo.service.AdminService;
import com.example.demo.service.AuthenticationService;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@RestController
@AllArgsConstructor
@Slf4j
@RequestMapping("/admin")

public class AdminController {

	private AdminService adminService;
	
	@PostMapping("/stylist")
	public void registerStylist(
			@RequestBody UserRegisterForm form,
			Authentication auth) {
		
		checkAuthority(auth);
		adminService.registerStylist(form);		
	}
	
	@PutMapping("/stylists/{id}/business-hours")
	public List<BusinessHour> registerBusinessHour(
			@PathVariable(name="id") long stylistId,
			@RequestBody BusinessHours businessHours,
			Authentication auth) {
		
		checkAuthority(auth);
		log.info("営業時間の設定処理開始");
		return adminService.registerBusinessHour(businessHours, stylistId);
	}
	
	@PostMapping("/menus")
	public Response<Menu> registerMenu(@RequestBody MenuRegisterForm menu,
			Authentication auth) {
		
		checkAuthority(auth);
		return adminService.registerMenu(menu);
	}
	@DeleteMapping("/stylist/{stylistId}")
	public void deleteStylist(@PathVariable long stylistId,
			Authentication auth) {
		
		checkAuthority(auth);
	    adminService.delete(stylistId);
	}
	
	@PutMapping("/stylists/{stylistId}/menus/{menuId}")
	public void assignMenu(@PathVariable long stylistId,
			@PathVariable long menuId,
			@RequestBody @Valid OriginalMenu originalMenu,
			Authentication auth) {
		
		checkAuthority(auth);
		adminService.assign(stylistId, menuId, originalMenu);
	}
	
	
	private void checkAuthority(Authentication auth) {
		if(!AuthenticationService.isAdmin(auth)) {
			throw new AccessDeniedException("管理者に依頼してください");
		};
	}
}
