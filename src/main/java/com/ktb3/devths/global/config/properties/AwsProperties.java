package com.ktb3.devths.global.config.properties;

import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "cloud.aws")
public class AwsProperties {
	private final Credentials credentials;
	private final Region region;
	private final S3 s3;
	private final Stack stack;

	@Getter
	@RequiredArgsConstructor
	public static class Credentials {
		private final String accessKey;
		private final String secretKey;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Region {
		private final String staticRegion;
	}

	@Getter
	@RequiredArgsConstructor
	public static class S3 {
		private final String bucket;
		private final String endpoint;
	}

	@Getter
	@RequiredArgsConstructor
	public static class Stack {
		private final boolean auto;
	}

}
