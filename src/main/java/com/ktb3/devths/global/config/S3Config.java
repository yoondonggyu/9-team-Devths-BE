package com.ktb3.devths.global.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;

import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.ktb3.devths.global.config.properties.AwsProperties;

import lombok.RequiredArgsConstructor;

@Configuration
@EnableConfigurationProperties(AwsProperties.class)
@RequiredArgsConstructor
public class S3Config {
	private final AwsProperties awsProperties;

	@Bean
	public AmazonS3 amazonS3() {
		BasicAWSCredentials credentials = new BasicAWSCredentials(
			awsProperties.getCredentials().getAccessKey(),
			awsProperties.getCredentials().getSecretKey()
		);

		AmazonS3ClientBuilder builder = AmazonS3ClientBuilder.standard()
			.withCredentials(new AWSStaticCredentialsProvider(credentials));

		String endpoint = awsProperties.getS3().getEndpoint();
		if (StringUtils.hasText(endpoint)) {
			// MinIO 등 S3 호환 온프레미스 스토리지 — path-style 접근 필수
			builder
				.withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(
					endpoint, awsProperties.getRegion().getStaticRegion()))
				.withPathStyleAccessEnabled(true);
		} else {
			builder.withRegion(awsProperties.getRegion().getStaticRegion());
		}

		return builder.build();
	}
}
