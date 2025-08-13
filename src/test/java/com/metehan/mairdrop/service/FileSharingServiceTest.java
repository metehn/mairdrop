package com.metehan.mairdrop.service;

import com.metehan.mairdrop.service.FileSharingService.FileSession;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.ConcurrentHashMap;
import static org.awaitility.reflect.WhiteboxImpl.getInternalState;
import static org.junit.jupiter.api.Assertions.*;

class FileSharingServiceTest {

    private FileSharingService service = new FileSharingService();

    @Test
    void testCleanupOldFiles_withWhitebox() throws IOException {
        service.init();

        MockMultipartFile file = new MockMultipartFile("file", "old.txt", "text/plain", "OldData".getBytes());
        String deviceId = "oldDevice";

        service.storeFile(deviceId, file);

        @SuppressWarnings("unchecked")
        ConcurrentHashMap<String, FileSession> fileSessions =
                (ConcurrentHashMap<String, FileSession>) getInternalState(service, "fileSessions");

        FileSession session = fileSessions.get(deviceId);
        session.setTimestamp(System.currentTimeMillis() - (25 * 60 * 60 * 1000));

        service.cleanupOldFiles();

        assertNull(service.getFileName(deviceId));
        assertFalse(Files.exists(Path.of(session.getFilePath())));
    }
}
