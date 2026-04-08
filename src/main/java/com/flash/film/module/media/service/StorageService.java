package com.flash.film.module.media.service;

import com.flash.film.common.enums.StorageDisk;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

public interface StorageService {

    String uploadFile(MultipartFile file, String folderName) throws IOException;
    void deleteFile(String storagePath);
    String getFileUrl(String storagePath);
    StorageDisk getStorageDisk();
}
