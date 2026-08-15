package com.ktb3.devths.global.storage.service;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import com.amazonaws.HttpMethod;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GeneratePresignedUrlRequest;
import com.ktb3.devths.global.config.properties.AwsProperties;
import com.ktb3.devths.global.storage.dto.request.PresignedUrlRequest;
import com.ktb3.devths.global.storage.dto.response.PresignedUrlResponse;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class S3StorageService {
	private static final long PRESIGNED_URL_EXPIRATION_MINUTES = 15;
	private static final String UPLOAD_PATH_PREFIX = "uploads";

	private final AmazonS3 amazonS3;
	private final AwsProperties awsProperties;

	public PresignedUrlResponse generatePresignedUrl(PresignedUrlRequest request) {
		String s3Key = generateS3Key(request.fileName());
		String presignedUrl = createPresignedUrl(s3Key, request.mimeType());

		return new PresignedUrlResponse(presignedUrl, s3Key);
	}

	private String generateS3Key(String originalFileName) {
		LocalDateTime now = LocalDateTime.now();
		String year = now.format(DateTimeFormatter.ofPattern("yyyy"));
		String month = now.format(DateTimeFormatter.ofPattern("MM"));
		String uuid = UUID.randomUUID().toString();

		return String.format("%s/%s/%s/%s_%s",
			UPLOAD_PATH_PREFIX,
			year,
			month,
			uuid,
			originalFileName
		);
	}

	public void deleteFile(String s3Key) {
		amazonS3.deleteObject(awsProperties.getS3().getBucket(), s3Key);
	}

	public String getPublicUrl(String s3Key) {
		String bucket = awsProperties.getS3().getBucket();
		String endpoint = awsProperties.getS3().getEndpoint();

		if (StringUtils.hasText(endpoint)) {
			return String.format("%s/%s/%s", endpoint, bucket, s3Key);
		}

		String region = awsProperties.getRegion().getStaticRegion();
		return String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, region, s3Key);
	}

	private String createPresignedUrl(String s3Key, String mimeType) {
		Date expiration = new Date(System.currentTimeMillis() + PRESIGNED_URL_EXPIRATION_MINUTES * 60 * 1000);

		GeneratePresignedUrlRequest generatePresignedUrlRequest = new GeneratePresignedUrlRequest(
			awsProperties.getS3().getBucket(),
			s3Key
		)
			.withMethod(HttpMethod.PUT)
			.withExpiration(expiration)
			.withContentType(mimeType);

		return amazonS3.generatePresignedUrl(generatePresignedUrlRequest).toString();
	}
}
