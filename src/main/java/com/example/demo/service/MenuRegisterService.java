package com.example.demo.service;


import org.springframework.stereotype.Service;

import com.example.demo.domain.dto.Id;
import com.example.demo.domain.dto.RegisterMenuForm;
import com.example.demo.domain.dto.Response;
import com.example.demo.domain.entity.Menu;
import com.example.demo.repository.MenuRepository;

import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@Slf4j
@AllArgsConstructor
public class MenuRegisterService {
	
	MenuRepository menuRepository;
	
	public Response<Menu> registerMenu(RegisterMenuForm form) {	
		menuRepository.insert(form);
	   Menu menu =  menuRepository.findByName(form.name());
	   log.info("{}", menu);
	   return new Response<>(menu);
	}
	
	public Menu getMenu(Id<Menu> menuId) {
		
		return menuRepository.findById(menuId).orElseGet(() -> {
			throw new IllegalArgumentException("存在しないメニューです。[id=" + menuId + "]");
		});
		
	}
	public void deleteMenu(Id<Menu> id) {
		menuRepository.delete(id);
	}
	public void updateMenu(RegisterMenuForm menu) {
		menuRepository.update(menu);
	}

}
