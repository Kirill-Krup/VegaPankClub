package com.actisys.userservice.util;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
@Getter
@Setter
@ConfigurationProperties(prefix = "yandex.cloud.s3")
public class YandexS3Properties {

    private String endpoint;
    private String region;
    private String bucket;
    private String accessKey;
    private String secretKey;
}
