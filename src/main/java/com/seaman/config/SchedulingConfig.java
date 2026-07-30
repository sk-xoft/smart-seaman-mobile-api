package com.seaman.config;

import com.seaman.service.DeleteUserMobileService;
import com.seaman.service.SendNotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

@Configuration
@EnableScheduling
@RequiredArgsConstructor
public class SchedulingConfig {


    private final SendNotificationService sendNotificationService;

    private final DeleteUserMobileService deleteUserMobileService;

    @Scheduled(cron = "${cache.scheduled.notification}")
    public void evictAllCachesAtIntervals() {
        sendNotificationService.sendNotification();

        deleteUserMobileService.deleteUserIsOverDueDate();
    }
}
