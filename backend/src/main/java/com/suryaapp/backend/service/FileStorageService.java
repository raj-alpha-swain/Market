package com.suryaapp.backend.service;

import com.suryaapp.backend.util.FileUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FileStorageService {

    private final FileUtil fileUtil;

    @Value("${server.port:8080}")
    private String serverPort;

    public String saveProductImage(MultipartFile file) throws IOException {
        if (!fileUtil.isValidImageFile(file)) {
            throw new IOException("Invalid image file type. Only JPEG, PNG, and WebP are allowed.");
        }

        String relativePath = fileUtil.saveFile(file, "products");
        return "http://localhost:" + serverPort + "/uploads" + relativePath;
    }

    public List<String> saveProductImages(List<MultipartFile> files) throws IOException {
        List<String> urls = new ArrayList<>();
        for (MultipartFile file : files) {
            urls.add(saveProductImage(file));
        }
        return urls;
    }

    public String saveCategoryIcon(MultipartFile file) throws IOException {
        if (!fileUtil.isValidImageFile(file)) {
            throw new IOException("Invalid image file type. Only JPEG, PNG, and WebP are allowed.");
        }

        String relativePath = fileUtil.saveFile(file, "categories");
        return "http://localhost:" + serverPort + "/uploads" + relativePath;
    }

    public String saveProductVideo(MultipartFile file) throws IOException {
        if (!fileUtil.isValidVideoFile(file)) {
            throw new IOException("Invalid video file type. Only MP4, MPEG, and QuickTime are allowed.");
        }

        String relativePath = fileUtil.saveFile(file, "products/videos");
        return "http://localhost:" + serverPort + "/uploads" + relativePath;
    }

    public void deleteFile(String fileUrl) {
        if (fileUrl != null && fileUrl.contains("/uploads/")) {
            String relativePath = fileUrl.substring(fileUrl.indexOf("/uploads/") + 8);
            fileUtil.deleteFile(relativePath);
        }
    }
}
