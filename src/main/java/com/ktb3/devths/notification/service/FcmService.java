package com.ktb3.devths.notification.service;

import java.util.List;

import org.springframework.lang.Nullable;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.google.firebase.messaging.FirebaseMessaging;
import com.google.firebase.messaging.FirebaseMessagingException;
import com.google.firebase.messaging.Message;
import com.google.firebase.messaging.MessagingErrorCode;
import com.google.firebase.messaging.WebpushConfig;
import com.google.firebase.messaging.WebpushNotification;
import com.ktb3.devths.notification.domain.entity.FcmToken;
import com.ktb3.devths.notification.repository.FcmTokenRepository;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
public class FcmService {

	private final FirebaseMessaging firebaseMessaging;
	private final FcmTokenRepository fcmTokenRepository;

	public FcmService(@Nullable FirebaseMessaging firebaseMessaging, FcmTokenRepository fcmTokenRepository) {
		this.firebaseMessaging = firebaseMessaging;
		this.fcmTokenRepository = fcmTokenRepository;
	}

	@Async("taskExecutor")
	@Transactional
	public void sendPushNotification(Long recipientId, String title, String body, String targetPath) {
		if (firebaseMessaging == null) {
			log.warn("FirebaseMessaging이 초기화되지 않아 FCM 전송을 건너뜁니다");
			return;
		}

		List<FcmToken> tokens = fcmTokenRepository.findAllByUserIdAndIsActiveTrue(recipientId);
		if (tokens.isEmpty()) {
			log.debug("FCM 토큰이 없어 전송을 건너뜁니다: userId={}", recipientId);
			return;
		}

		for (FcmToken fcmToken : tokens) {
			try {
				Message message = Message.builder()
					.setToken(fcmToken.getToken())
					.setWebpushConfig(WebpushConfig.builder()
						.setNotification(WebpushNotification.builder()
							.setTitle(title)
							.setBody(body)
							.build())
						.putData("targetPath", targetPath)
						.build())
					.build();

				firebaseMessaging.send(message);
				log.debug("FCM 전송 성공: userId={}, tokenId={}", recipientId, fcmToken.getId());
			} catch (FirebaseMessagingException e) {
				handleMessagingException(fcmToken, recipientId, e);
			} catch (Exception e) {
				log.error("FCM 전송 중 예상치 못한 오류: userId={}, tokenId={}", recipientId, fcmToken.getId(), e);
			}
		}
	}

	private void handleMessagingException(FcmToken fcmToken, Long recipientId, FirebaseMessagingException ex) {
		MessagingErrorCode errorCode = ex.getMessagingErrorCode();

		if (errorCode == MessagingErrorCode.UNREGISTERED
			|| errorCode == MessagingErrorCode.INVALID_ARGUMENT) {
			fcmTokenRepository.deleteByToken(fcmToken.getToken());
			log.info("만료된 FCM 토큰 삭제: userId={}, tokenId={}", recipientId, fcmToken.getId());
		} else {
			log.warn("FCM 전송 실패: userId={}, errorCode={}", recipientId, errorCode);
		}
	}
}
