package com.example.demo.restcontroller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.Role;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.User;
import com.example.demo.service.StylistMenuService;
import com.example.demo.service.TimeRange;
import com.example.demo.service.UserService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/stylists")
public class StylistMenuViewController {

    private final UserService userService;
    private final StylistMenuService stylistService;
    
    Role STYLIST = Role.STYLIST;
	
	public StylistMenuViewController(UserService userService,
			StylistMenuService stylistService) {
		this.userService = userService;
		this.stylistService = stylistService;
	}
	
	
	@GetMapping
	public List<User> list() {
		return userService.findUser(STYLIST.name());
	}
	
	@GetMapping("/{id}/menus")
	public List<Menu> everyStylistMenuList(
			@PathVariable long id) {

		return stylistService.getMenuForStylist(id);
		
	}
	
	@GetMapping("/{id}/availability")
	public List<TimeRange> emptyTimeList(
			@PathVariable long id,
			@RequestParam LocalDateTime date,
			@RequestParam long menuId) {
		
		return stylistService.getList(id, date, menuId);
		
	}
	
	
}
