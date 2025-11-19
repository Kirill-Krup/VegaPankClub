package com.actisys.userservice.service.impl;

import com.actisys.userservice.service.UserPhotoStorageService;
import com.actisys.userservice.util.YandexS3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserPhotoStorageServiceImpl implements UserPhotoStorageService {

    private final S3Client s3Client;
    private final YandexS3Properties props;

    @Override
    public String uploadUserPhoto(Long userId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String key = "users/" + userId + "/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Uploaded photo for user {} to bucket {} with key {}", userId, props.getBucket(), key);

            return "https://" + props.getBucket() + ".storage.yandexcloud.net/" + key;
        }
        catch (IOException e) {
            throw new RuntimeException("Failed to upload user photo", e);
        }
    }
}
