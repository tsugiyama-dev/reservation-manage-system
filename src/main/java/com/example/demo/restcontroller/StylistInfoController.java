package com.example.demo.restcontroller;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.TimeRange;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.StylistResponse;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.service.StylistMenuService;

import lombok.extern.slf4j.Slf4j;

@RestController
@Slf4j
@RequestMapping("/stylists")
public class StylistInfoController {


    private final StylistMenuService stylistService;
	
	public StylistInfoController(StylistMenuService stylistService) {
		this.stylistService = stylistService;
	}
	@GetMapping
	public List<StylistResponse> list() {
		return stylistService.getStylists();
	}
	
	@GetMapping("/{id}/menus")
	public List<Menu> stylistMenuList(
			@PathVariable long id) {

		return stylistService.getMenuByStylist(id);
	}
	@GetMapping("/{id}/availability")
	public List<TimeRange> emptyTimeList(
			@PathVariable long id,
			@RequestParam LocalDateTime date,
			@RequestParam long menuId) {
		
		return stylistService.getAvalilableEmptySlot(new Id<Stylist>(id), date, new Id<Menu>(menuId), null);
	}
}
