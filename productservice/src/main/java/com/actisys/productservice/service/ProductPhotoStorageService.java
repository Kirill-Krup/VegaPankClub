package com.actisys.productservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface ProductPhotoStorageService {
    String uploadProductPhoto(Long productId, MultipartFile file);

    void deleteProductPhoto(String photoUrl);

}
