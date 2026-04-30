package com.manga.library.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class FileStorageService {

    @Value("${file.upload-dir}")
    private String uploadDir;

    public List<String> extractZipAndSaveImages(MultipartFile zipFile, Long mangaId, Double chapterNumber) {
        List<String> savedFilesPaths = new ArrayList<>();

        // Створюємо шлях: uploads/manga_{id}/chapter_{number}
        String chapterDirName = "manga_" + mangaId + "/chapter_" + chapterNumber;
        Path targetDir = Paths.get(uploadDir, chapterDirName);

        try {
            Files.createDirectories(targetDir);

            // Читаємо ZIP
            try (ZipInputStream zis = new ZipInputStream(zipFile.getInputStream())) {
                ZipEntry zipEntry = zis.getNextEntry();

                while (zipEntry != null) {
                    // Ігноруємо папки всередині архіву і не-картинки
                    if (!zipEntry.isDirectory() && isImage(zipEntry.getName())) {
                        String fileName = new File(zipEntry.getName()).getName();
                        Path filePath = targetDir.resolve(fileName);

                        // Зберігаємо файл
                        try (FileOutputStream fos = new FileOutputStream(filePath.toFile())) {
                            byte[] buffer = new byte[1024];
                            int len;
                            while ((len = zis.read(buffer)) > 0) {
                                fos.write(buffer, 0, len);
                            }
                        }
                        // Зберігаємо відносний шлях для бази даних
                        savedFilesPaths.add(chapterDirName + "/" + fileName);
                    }
                    zipEntry = zis.getNextEntry();
                }
                zis.closeEntry();
            }
        } catch (IOException e) {
            throw new RuntimeException("Помилка при розпакуванні ZIP архіву: " + e.getMessage());
        }

        // Сортуємо файли за алфавітом (щоб 01.jpg був перед 02.jpg)
        savedFilesPaths.sort(Comparator.naturalOrder());
        return savedFilesPaths;
    }

    private boolean isImage(String fileName) {
        String lowerCaseName = fileName.toLowerCase();
        return lowerCaseName.endsWith(".jpg") || lowerCaseName.endsWith(".jpeg") ||
                lowerCaseName.endsWith(".png") || lowerCaseName.endsWith(".webp");
    }
}