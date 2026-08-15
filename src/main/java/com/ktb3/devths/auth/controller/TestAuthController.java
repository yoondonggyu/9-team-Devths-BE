package com.ktb3.devths.auth.controller;

import org.springframework.context.annotation.Profile;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.ktb3.devths.auth.dto.internal.TokenPair;
import com.ktb3.devths.auth.service.JwtTokenService;
import com.ktb3.devths.auth.util.CookieUtil;
import com.ktb3.devths.user.domain.entity.User;
import com.ktb3.devths.user.domain.constant.UserRoles;
import com.ktb3.devths.user.repository.UserRepository;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * Google OAuth 없이 JWT를 바로 발급하는 테스트 전용 로그인.
 * ADR-003 — prod 프로파일에서는 반드시 비활성화.
 */
@Profile("!prod")
@RestController
@RequestMapping("/api/test-auth")
@RequiredArgsConstructor
public class TestAuthController {

	private final JwtTokenService jwtTokenService;
	private final UserRepository userRepository;

	@PostMapping("/login")
	public ResponseEntity<String> testLogin(
		@RequestParam(defaultValue = "test@devths.local") String email,
		HttpServletResponse response
	) {
		User user = userRepository.findByEmail(email)
			.orElseGet(() -> userRepository.save(
				User.builder()
					.email(email)
					.nickname("테스트유저")
					.role(UserRoles.ROLE_USER)
					.build()
			));

		TokenPair tokenPair = jwtTokenService.issueTokenPair(user);

		response.setHeader("Authorization", "Bearer " + tokenPair.accessToken());
		response.addCookie(CookieUtil.createRefreshTokenCookie(tokenPair.refreshToken()));

		return ResponseEntity.ok(tokenPair.accessToken());
	}
}
