package com.example.demo.restcontroller;

import jakarta.validation.Valid;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.example.demo.domain.dto.ReviewPostForm;
import com.example.demo.service.ReViewService;
import com.example.demo.service.StylistEvaluateResult;

import lombok.AllArgsConstructor;

@RestController
@AllArgsConstructor
public class ReviewPostController {

	ReViewService reviewService;
	
	@PostMapping("/reservations/{id}/review")
	public void post(@RequestBody @Valid ReviewPostForm form,
			Authentication auth,
			@PathVariable long id) {
		String email = (String)auth.getPrincipal();
		reviewService.post(email, form, id);
	}
	
	@GetMapping("/stylists/{id}/reviews")
	public StylistEvaluateResult getReview(@PathVariable long id) {
		return reviewService.getReview(id);
	}
}
