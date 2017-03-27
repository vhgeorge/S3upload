package com.verne.assignment.service;

import com.verne.assignment.model.Video;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.security.NoSuchAlgorithmException;

public interface FileUploadService {
    Video uploadFile(MultipartFile file, byte[] bytes);
}
