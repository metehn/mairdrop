package com.metehan.mairdrop.controller;

import com.metehan.mairdrop.model.FileTransferRequest;
import com.metehan.mairdrop.service.FileSharingService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;


@Controller
public class FileShareController {

    private static final Logger log = LoggerFactory.getLogger(FileShareController.class);

    private final FileSharingService fileSharingService;
    private final SimpMessagingTemplate messagingTemplate;

    public FileShareController(FileSharingService fileSharingService, SimpMessagingTemplate messagingTemplate) {
        this.fileSharingService = fileSharingService;
        this.messagingTemplate = messagingTemplate;
    }

    @PostMapping("/upload")
    public String handleFileUpload(@RequestParam("file") MultipartFile file,
                                   @RequestParam("targetDeviceId") String targetDeviceId,
                                   @RequestParam("senderDeviceId") String senderDeviceId) {
        try {
            if (file.isEmpty()) {
                return "redirect:/?error=empty_file";
            }

            fileSharingService.storeFile(targetDeviceId, file);

            FileTransferRequest request = new FileTransferRequest();
            request.setSenderDeviceId(senderDeviceId);
            request.setTargetDeviceId(targetDeviceId);
            request.setFileName(file.getOriginalFilename());

            messagingTemplate.convertAndSend(
                    "/topic/file-request/" + targetDeviceId,
                    request
            );

            return "redirect:/?success=file_sent";

        } catch (IOException e) {
            log.error(e.getMessage());
            return "redirect:/?error=upload_failed";
        }
    }

    @GetMapping("/download")
    public ResponseEntity<byte[]> downloadFile(@RequestParam("deviceId") String deviceId) {
        try {
            byte[] fileData = fileSharingService.getFile(deviceId);
            String fileName = fileSharingService.getFileName(deviceId);

            if (fileData == null) {
                return ResponseEntity.notFound().build();
            }

            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_OCTET_STREAM);
            headers.setContentDispositionFormData("attachment",
                    fileName != null ? fileName : "downloaded_file");
            headers.setContentLength(fileData.length);

            return new ResponseEntity<>(fileData, headers, HttpStatus.OK);

        } catch (IOException e) {
            log.error(e.getMessage());
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }
}