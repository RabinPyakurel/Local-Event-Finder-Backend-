package com.rabin.backend.util;

import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.Arrays;
import java.util.List;

public class FileUtil {
    private FileUtil(){}

    private static final String UPLOAD_DIR = "uploads";
    private static final long MAX_FILE_SIZE = 5 * 1024 * 1024; // 5MB
    private static final List<String> ALLOWED_EXTENSIONS = Arrays.asList("jpg", "jpeg", "png", "gif", "webp");
    private static final List<String> ALLOWED_CONTENT_TYPES = Arrays.asList(
        "image/jpeg", "image/jpg", "image/png", "image/gif", "image/webp"
    );

    public static String saveFile(MultipartFile file, String folder) {
        if (file == null || file.isEmpty()) {
            return null;
        }

        // Validate file size
        if (file.getSize() > MAX_FILE_SIZE) {
            throw new IllegalArgumentException("File size exceeds maximum limit of 5MB");
        }

        // Validate content type
        String contentType = file.getContentType();
        if (contentType == null || !ALLOWED_CONTENT_TYPES.contains(contentType.toLowerCase())) {
            throw new IllegalArgumentException("Invalid file type. Only images are allowed (JPG, PNG, GIF, WEBP)");
        }

        // Validate file extension
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isEmpty()) {
            throw new IllegalArgumentException("Invalid filename");
        }

        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!ALLOWED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Invalid file extension. Only images are allowed");
        }

        // Prevent directory traversal
        if (originalFilename.contains("..") || originalFilename.contains("/") || originalFilename.contains("\\")) {
            throw new IllegalArgumentException("Invalid filename: path traversal detected");
        }

        try {
            // Get the absolute path to the project root
            String projectRoot = System.getProperty("user.dir");
            Path uploadPath = Paths.get(projectRoot, UPLOAD_DIR, folder).toAbsolutePath();

            // Create directories if they don't exist
            if (!Files.exists(uploadPath)) {
                Files.createDirectories(uploadPath);
                System.out.println("✅ Created directory: " + uploadPath);
            }

            // Generate unique file name with sanitized extension
            String sanitizedBaseName = originalFilename.replaceAll("\\s+", "_").replaceAll("[^a-zA-Z0-9._-]", "");
            String fileName = System.currentTimeMillis() + "_" + sanitizedBaseName;
            Path filePath = uploadPath.resolve(fileName);

            // Copy file content
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
            
            System.out.println("✅ File saved to: " + filePath);

            // Return relative URL for frontend
            return "/" + UPLOAD_DIR + "/" + folder + "/" + fileName;
        } catch (Exception e) {
            System.err.println("❌ Failed to save file: " + e.getMessage());
            throw new RuntimeException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private static String getFileExtension(String filename) {
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1 || lastDotIndex == filename.length() - 1) {
            return "";
        }
        return filename.substring(lastDotIndex + 1);
    }
}
