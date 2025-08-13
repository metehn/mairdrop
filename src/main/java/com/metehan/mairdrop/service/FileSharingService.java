package com.metehan.mairdrop.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class FileSharingService {

    private final ConcurrentHashMap<String, FileSession> fileSessions = new ConcurrentHashMap<>();
    private final Path uploadPath = Paths.get("temp-uploads");

    @PostConstruct
    public void init() throws IOException {
        Files.createDirectories(uploadPath);
    }

    public void storeFile(String deviceId, MultipartFile file) throws IOException {
        System.out.println("storeFile çağrıldı - deviceId: " + deviceId + ", dosya: " + file.getOriginalFilename());

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = uploadPath.resolve(fileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);
        System.out.println("Dosya diske yazıldı: " + filePath);

        FileSession session = new FileSession(deviceId);
        session.setFilePath(filePath.toString());
        session.setFileName(file.getOriginalFilename());
        session.setTimestamp(System.currentTimeMillis());

        fileSessions.put(deviceId, session);
        System.out.println("FileSession oluşturuldu ve kaydedildi");
    }

    public byte[] getFile(String deviceId) throws IOException {
        FileSession session = fileSessions.get(deviceId);
        if (session != null) {
            Path filePath = Paths.get(session.getFilePath());
            if (Files.exists(filePath)) {
                byte[] fileData = Files.readAllBytes(filePath);

                Files.deleteIfExists(filePath);
                fileSessions.remove(deviceId);

                return fileData;
            }
        }
        return null;
    }

    public String getFileName(String deviceId) {
        FileSession session = fileSessions.get(deviceId);
        return session != null ? session.getFileName() : null;
    }

    public void cleanupOldFiles() {
        long currentTime = System.currentTimeMillis();
        long maxAge = 24 * 60 * 60 * 1000; // 24 saat

        fileSessions.entrySet().removeIf(entry -> {
            FileSession session = entry.getValue();
            if (currentTime - session.getTimestamp() > maxAge) {
                try {
                    Files.deleteIfExists(Paths.get(session.getFilePath()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                return true;
            }
            return false;
        });
    }

    public static class FileSession {
        private String deviceId;
        private String filePath;
        private String fileName;
        private long timestamp;

        public FileSession(String deviceId) {
            this.deviceId = deviceId;
        }

        public String getDeviceId() {
            return deviceId;
        }

        public String getFilePath() {
            return filePath;
        }

        public void setFilePath(String filePath) {
            this.filePath = filePath;
        }

        public String getFileName() {
            return fileName;
        }

        public void setFileName(String fileName) {
            this.fileName = fileName;
        }

        public long getTimestamp() {
            return timestamp;
        }

        public void setTimestamp(long timestamp) {
            this.timestamp = timestamp;
        }
    }
}