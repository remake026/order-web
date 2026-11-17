package com.example.ordering.controller;

import com.example.ordering.exception.ApiException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * 菜品图片上传。文件落到 ${upload.dir}（容器内 /app/uploads），
 * 该目录通过 Docker volume 映射到宿主机 ./uploads，容器重启不丢图。
 */
@RestController
@RequestMapping("/api/upload")
public class UploadController {

    private static final Set<String> ALLOWED_EXT = Set.of("jpg", "jpeg", "png", "webp", "gif");

    @Value("${upload.dir}")
    private String uploadDir;

    @PostMapping
    public Map<String, String> upload(@RequestParam("file") MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new ApiException("请选择要上传的图片");
        }

        String original = file.getOriginalFilename();
        String ext = "";
        if (original != null && original.lastIndexOf('.') >= 0) {
            ext = original.substring(original.lastIndexOf('.') + 1).toLowerCase();
        }
        if (!ALLOWED_EXT.contains(ext)) {
            throw new ApiException("仅支持 jpg / jpeg / png / webp / gif 格式");
        }

        String filename = UUID.randomUUID().toString().replace("-", "") + "." + ext;
        try {
            Path dir = Paths.get(uploadDir).toAbsolutePath();
            Files.createDirectories(dir);
            file.transferTo(dir.resolve(filename).toFile());
            return Map.of("url", "/uploads/" + filename);
        } catch (IOException e) {
            throw new ApiException("图片保存失败：" + e.getMessage());
        }
    }
}
