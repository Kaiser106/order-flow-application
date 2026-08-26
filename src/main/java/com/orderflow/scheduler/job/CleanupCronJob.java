package com.orderflow.scheduler.job;

import com.orderflow.scheduler.service.CleanupService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CleanupCronJob {

    private final CleanupService cleanupService;

    /**
     * Cron ifadesi (Saniye Dakika Saat Gün Ay HaftanınGünü)
     * "0 0 3 * * ?" -> Her gece saat 03:00'te çalışır.
     * Neden gece 3? Sunucu trafiğinin en düşük olduğu saatlerde ağır veritabanı
     * operasyonlarını yapmak performansı korur.
     */
    @Scheduled(cron = "0 0 3 * * ?")
    public void cleanUpSoftDeletedRecords() {
        cleanupService.performHardDelete();
    }
}