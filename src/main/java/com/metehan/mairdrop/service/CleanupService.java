package com.metehan.mairdrop.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;


@Service
public class CleanupService {

    private static final Logger log = LoggerFactory.getLogger(CleanupService.class);

    private final DeviceService deviceService;
    private final FileSharingService fileSharingService;

    public CleanupService(DeviceService deviceService, FileSharingService fileSharingService) {
        this.deviceService = deviceService;
        this.fileSharingService = fileSharingService;
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupInactiveDevices() {
        log.info("Cleanup inactive devices...");
        deviceService.cleanupInactiveDevices();
    }

    @Scheduled(fixedRate = 300000)
    public void cleanupOldFiles() {
        log.info("Cleanup old files...");
        fileSharingService.cleanupOldFiles();
    }
}
