package com.example.demo.service;

import java.util.List;
import java.util.function.Supplier;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.example.demo.domain.Role;
import com.example.demo.domain.dto.BusinessHour;
import com.example.demo.domain.dto.BusinessHours;
import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.MenuRegisterForm;
import com.example.demo.domain.dto.OriginalMenu;
import com.example.demo.domain.dto.Response;
import com.example.demo.domain.dto.UserRegisterForm;
import com.example.demo.domain.entity.Menu;
import com.example.demo.domain.entity.Stylist;
import com.example.demo.domain.entity.StylistMenu;
import com.example.demo.domain.entity.User;
import com.example.demo.repository.BusinessHourRepository;
import com.example.demo.repository.MenuRepository;
import com.example.demo.repository.StylistMenuRepository;
import com.example.demo.repository.StylistRepository;
import com.example.demo.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@RequiredArgsConstructor
public class AdminService {
	
	private final StylistMenuRepository stylistMenuRepository;
	private final MenuRepository menuRepository;
	private final BCryptPasswordEncoder passwordEncoder;
	private final UserRepository userRepository;
	private final StylistRepository stylistRepository;
	private final BusinessHourRepository businessHourRepository;
	
	@Transactional
	public void registerStylist(UserRegisterForm form) {
		
    	String encoded = passwordEncoder.encode(form.password());
    	
    	User user = new User();
    	user.setEmail(form.email());
    	user.setName(form.username());
    	user.setPasswordHash(encoded);
    	user.setRole(Role.STYLIST);
    	
    	userRepository.insert(user);
		stylistRepository.insert(user.getId(),form.bio() != null ? form.bio() : null);
	}
	@Transactional
	public void delete(long id) {
		findStylistById(new Id<Stylist>(id));
		deleteStylistById(new Id<Stylist>(id));
		deleteUserById(id);
	}

	public List<BusinessHour> registerBusinessHour(BusinessHours businessHours, long stylistId) {
		if(businessHourRepository.findById(new Id<Stylist>(stylistId)) != null) {
			businessHourRepository.delete(stylistId);
		}
		for(BusinessHour e : businessHours.businessHours()) {
			businessHourRepository.insert(stylistId, e);
		}
		return businessHourRepository.findById(new Id<Stylist>(stylistId));
	}
	public Response<Menu> registerMenu(MenuRegisterForm form) {
		Menu menu = MenuRegisterForm.toMenu(form);
		menuRepository.insert(menu);
	    return new Response<>(menu);
	}
	public void assign(long stylistId, long menuId, OriginalMenu original) {
		Menu menu = getMenuById(new Id<Menu>(menuId));
		menu = original != null ? setOriginalMenu(menu, original) : menu;
		save(menu, stylistId);
	}
	
	
	private Stylist findStylistById(Id<Stylist> stylistId) {
		return stylistRepository.findById(stylistId).orElseThrow(stylistNotFoundExceptionSupplier());
	}
	private void deleteUserById(long userId) {
		userRepository.delete(userId);
	}	
	private void deleteStylistById(Id<Stylist> stylistId) {
		stylistRepository.delete(stylistId);
	}
	private Supplier<IllegalArgumentException> stylistNotFoundExceptionSupplier() {
		return () -> new IllegalArgumentException("指定されたスタイリストは登録されていません");
	}


	private Menu setOriginalMenu(Menu menu, OriginalMenu original) {
		menu.setBaseDurationMinutes(original.durationMinutes().intValue());
		menu.setBasePrice(original.price().intValue());
		return menu;
	}
	private Menu getMenuById(Id<Menu> id) {
		return menuRepository.findById(id).orElseThrow(menuNotFoundExceptionSupplier());
	}
	private void save(Menu menu, long stylistId) {
		var standardMenu = new StylistMenu(stylistId, menu.getId(), menu.getBaseDurationMinutes(),menu.getBasePrice());
		stylistMenuRepository.insert(standardMenu);
	}
	private Supplier<IllegalArgumentException> menuNotFoundExceptionSupplier() {
		return () -> new IllegalArgumentException("指定されたメニューは登録されていません");
	}
}
