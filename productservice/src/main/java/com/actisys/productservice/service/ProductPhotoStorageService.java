package com.actisys.productservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProductPhotoStorageService {

    /**
     * Uploads product photo to Yandex S3 bucket and returns public access URL.
     * Generates unique key with productId prefix and original filename suffix.
     * Uses content-type from uploaded file for proper storage.
     *
     * @param productId identifier of product the photo belongs to
     * @param file MultipartFile containing photo data
     * @return full public URL to uploaded photo
     */
    String uploadProductPhoto(Long productId, MultipartFile file);

    /**
     * Deletes product photo from Yandex S3 bucket by public URL.
     * Extracts bucket and key from URL, validates bucket ownership before deletion.
     * Ignores invalid or empty URLs silently.
     *
     * @param photoUrl full public URL of photo to delete
     */
    void deleteProductPhoto(String photoUrl);
}
