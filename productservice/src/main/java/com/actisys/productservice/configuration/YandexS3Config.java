package com.actisys.productservice.configuration;

import com.actisys.productservice.util.YandexS3Properties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;

@Configuration
@EnableConfigurationProperties(YandexS3Properties.class)
public class YandexS3Config {

    @Bean
    public S3Client s3Client(YandexS3Properties props) {
        AwsBasicCredentials awsCreds = AwsBasicCredentials.create(
                props.getAccessKey(),
                props.getSecretKey()
        );

        return S3Client.builder()
                .endpointOverride(URI.create(props.getEndpoint()))
                .credentialsProvider(StaticCredentialsProvider.create(awsCreds))
                .region(Region.of(props.getRegion()))
                .build();
    }
}

