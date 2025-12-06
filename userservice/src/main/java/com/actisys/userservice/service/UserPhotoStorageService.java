package com.actisys.userservice.service;

import org.springframework.web.multipart.MultipartFile;

public interface UserPhotoStorageService {
    /**
     * This method go to api server for save new user photo
     * */
    String uploadUserPhoto(Long userId, MultipartFile file);
}
