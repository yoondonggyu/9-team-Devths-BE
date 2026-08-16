package com.ktb3.devths.global.config;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.lang.Nullable;

import com.google.auth.oauth2.GoogleCredentials;
import com.google.firebase.FirebaseApp;
import com.google.firebase.FirebaseOptions;
import com.google.firebase.messaging.FirebaseMessaging;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Configuration
public class FirebaseConfig {

	@Bean
	public FirebaseApp firebaseApp() throws IOException {
		String credentialsJson = System.getenv("FIREBASE_CREDENTIALS_JSON");

		if (credentialsJson == null || credentialsJson.isBlank()) {
			log.warn("FIREBASE_CREDENTIALS_JSON 환경변수가 설정되지 않았습니다. FCM 푸시 알림이 비활성화됩니다.");
			return null;
		}

		GoogleCredentials credentials = GoogleCredentials.fromStream(
			new ByteArrayInputStream(credentialsJson.getBytes(StandardCharsets.UTF_8))
		);

		FirebaseOptions options = FirebaseOptions.builder()
			.setCredentials(credentials)
			.build();

		log.info("FirebaseApp 초기화 완료");
		return FirebaseApp.initializeApp(options);
	}

	@Bean
	public FirebaseMessaging firebaseMessaging(@Nullable FirebaseApp firebaseApp) {
		if (firebaseApp == null) {
			return null;
		}
		return FirebaseMessaging.getInstance(firebaseApp);
	}
}
