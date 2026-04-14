package com.yourteam.ojaitester.util;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

public class FileUtils {

    public static String saveUploadedFile(File sourceFile, String folderName) throws IOException {
        Path targetDir = Path.of("uploads", folderName);
        Files.createDirectories(targetDir);

        Path targetPath = targetDir.resolve(sourceFile.getName());
        Files.copy(sourceFile.toPath(), targetPath, StandardCopyOption.REPLACE_EXISTING);

        return targetPath.toAbsolutePath().toString();
    }

    public static boolean isPdf(File file) {
        return file != null && file.getName().toLowerCase().endsWith(".pdf");
    }

    public static boolean isImage(File file) {
        if (file == null) return false;
        String name = file.getName().toLowerCase();
        return name.endsWith(".png")
                || name.endsWith(".jpg")
                || name.endsWith(".jpeg")
                || name.endsWith(".bmp")
                || name.endsWith(".webp");
    }
}