package com.actisys.productservice.service.impl;

import com.actisys.productservice.repository.ProductRepository;
import com.actisys.productservice.service.ProductPhotoStorageService;
import com.actisys.productservice.util.YandexS3Properties;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import java.io.IOException;
import java.net.URI;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class ProductPhotoStorageServiceImpl implements ProductPhotoStorageService {

    private final S3Client s3Client;
    private final YandexS3Properties props;
    private final ProductRepository productRepository;

    @Override
    public String uploadProductPhoto(Long productId, MultipartFile file) {
        String originalFilename = file.getOriginalFilename();
        String key = "product/" + productId + "/" + UUID.randomUUID() + "-" + originalFilename;

        try {
            PutObjectRequest putObjectRequest = PutObjectRequest.builder()
                    .bucket(props.getBucket())
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3Client.putObject(putObjectRequest, RequestBody.fromBytes(file.getBytes()));
            log.info("Uploaded photo for product {} to bucket {} with key {}", productId, props.getBucket(), key);

            return "https://" + props.getBucket() + ".storage.yandexcloud.net/" + key;
        } catch (IOException e) {
            throw new RuntimeException("Failed to upload product photo", e);
        }
    }

    @Override
    public void deleteProductPhoto(String photoUrl) {
        if (photoUrl == null || photoUrl.isBlank()) {
            return;
        }
        try {
            URI uri = new URI(photoUrl);
            String host = uri.getHost();
            String path = uri.getPath();

            if (!host.startsWith(props.getBucket() + ".")) {
                log.warn("Photo URL {} does not belong to bucket {}", photoUrl, props.getBucket());
                return;
            }

            String key = path.startsWith("/") ? path.substring(1) : path;
            s3Client.deleteObject(builder -> builder
                    .bucket(props.getBucket())
                    .key(key)
            );
            log.info("Deleted photo from bucket {} with key {}", props.getBucket(), key);
        } catch (Exception e) {
            log.error("Failed to delete product photo {}: {}", photoUrl, e.getMessage(), e);
        }
    }
}

