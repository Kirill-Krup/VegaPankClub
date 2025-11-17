package com.actisys.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserPhotoStorageService {
    String uploadUserPhoto(Long userId, MultipartFile file);
}
