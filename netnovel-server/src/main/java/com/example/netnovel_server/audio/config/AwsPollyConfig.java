package com.example.netnovel_server.audio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.DefaultCredentialsProvider;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.polly.PollyClient;
import software.amazon.awssdk.services.polly.PollyClientBuilder;

@Configuration
public class AwsPollyConfig {

    @Bean
    public PollyClient pollyClient(AudioProperties properties) {
        PollyClientBuilder builder = PollyClient.builder()
            .region(Region.of(properties.getAwsRegion()));

        if (hasStaticCredentials(properties)) {
            builder.credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(
                    properties.getAwsAccessKeyId(),
                    properties.getAwsSecretAccessKey()
                )
            ));
        } else {
            builder.credentialsProvider(DefaultCredentialsProvider.create());
        }

        return builder.build();
    }

    private boolean hasStaticCredentials(AudioProperties properties) {
        return properties.getAwsAccessKeyId() != null
            && !properties.getAwsAccessKeyId().isBlank()
            && properties.getAwsSecretAccessKey() != null
            && !properties.getAwsSecretAccessKey().isBlank();
    }
}
