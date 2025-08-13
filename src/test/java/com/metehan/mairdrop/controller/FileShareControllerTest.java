package com.metehan.mairdrop.controller;

import com.metehan.mairdrop.model.FileTransferRequest;
import com.metehan.mairdrop.service.FileSharingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class FileShareControllerTest {

    private FileSharingService fileSharingService;
    private SimpMessagingTemplate messagingTemplate;
    private FileShareController controller;

    @BeforeEach
    void setUp() {
        fileSharingService = mock(FileSharingService.class);
        messagingTemplate = mock(SimpMessagingTemplate.class);
        controller = new FileShareController(fileSharingService, messagingTemplate);
    }

    @Test
    void testIndex() {
        String view = controller.index();
        assertEquals("index", view);
    }

    @Test
    void testHandleFileUpload_emptyFile() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(true);

        String result = controller.handleFileUpload(file, "target1", "sender1");
        assertEquals("redirect:/?error=empty_file", result);

        verifyNoInteractions(fileSharingService);
        verifyNoInteractions(messagingTemplate);
    }

    @Test
    void testHandleFileUpload_success() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        when(file.getOriginalFilename()).thenReturn("test.txt");

        String result = controller.handleFileUpload(file, "target1", "sender1");
        assertEquals("redirect:/?success=file_sent", result);

        verify(fileSharingService).storeFile("target1", file);

        ArgumentCaptor<FileTransferRequest> captor = ArgumentCaptor.forClass(FileTransferRequest.class);
        verify(messagingTemplate).convertAndSend(eq("/topic/file-request/target1"), captor.capture());

        FileTransferRequest request = captor.getValue();
        assertEquals("sender1", request.getSenderDeviceId());
        assertEquals("target1", request.getTargetDeviceId());
        assertEquals("test.txt", request.getFileName());
    }

    @Test
    void testHandleFileUpload_ioException() throws IOException {
        MultipartFile file = mock(MultipartFile.class);
        when(file.isEmpty()).thenReturn(false);
        doThrow(new IOException("Failed")).when(fileSharingService).storeFile(anyString(), any());

        String result = controller.handleFileUpload(file, "target1", "sender1");
        assertEquals("redirect:/?error=upload_failed", result);
    }

    @Test
    void testDownloadFile_success() throws IOException {
        byte[] data = "hello".getBytes();
        when(fileSharingService.getFile("device1")).thenReturn(data);
        when(fileSharingService.getFileName("device1")).thenReturn("file.txt");

        ResponseEntity<byte[]> response = controller.downloadFile("device1");
        assertEquals(HttpStatus.OK, response.getStatusCode());
        assertArrayEquals(data, response.getBody());
        assertEquals("file.txt", response.getHeaders().getContentDisposition().getFilename());
    }

    @Test
    void testDownloadFile_notFound() throws IOException {
        when(fileSharingService.getFile("device1")).thenReturn(null);

        ResponseEntity<byte[]> response = controller.downloadFile("device1");
        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
    }

    @Test
    void testDownloadFile_ioException() throws IOException {
        when(fileSharingService.getFile("device1")).thenThrow(new IOException("Fail"));

        ResponseEntity<byte[]> response = controller.downloadFile("device1");
        assertEquals(HttpStatus.INTERNAL_SERVER_ERROR, response.getStatusCode());
    }
}
