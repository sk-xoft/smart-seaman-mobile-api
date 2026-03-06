package com.seaman.config;

import com.seaman.service.DeleteUserMobileService;
import com.seaman.service.SendNotificationService;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {

    private final Logger logger = LoggerFactory.getLogger(SchedulingConfig.class);

    private final SendNotificationService sendNotificationService;

    private final DeleteUserMobileService deleteUserMobileService;

    @Scheduled(cron = "${cache.scheduled.notification}")
    public void evictAllCachesAtIntervals() {
        sendNotificationService.sendNotification();

        deleteUserMobileService.deleteUserIsOverDueDate();
    }
}
