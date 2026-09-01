package com.orderflow.scheduler.service;

import com.orderflow.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class CleanupService {

    // Hibernate'in @SQLRestriction kalkanını aşıp doğrudan SQL çalıştırmak için
    private final JdbcTemplate jdbcTemplate;
    private final LoggingService loggingService;

    // application.yaml dosyasındaki app.cleanup.soft-delete-retention-days değerini okur (Şu an 30)
    // Magic number (sihirli rakam) kullanmamak için bu değeri dosyadan alıyoruz.
    @Value("${app.cleanup.soft-delete-retention-days}")
    private int retentionDays;

    @Transactional
    public void performHardDelete() {
        // Bugünden 30 gün öncesini hesaplıyoruz
        OffsetDateTime thresholdDate = OffsetDateTime.now().minusDays(retentionDays);

        log.info("Hard delete cleanup started for records older than {} days ({})", retentionDays, thresholdDate);

        try {
            // Sadece deleted_at tarihi 30 günden eski olanları kalıcı olarak siliyoruz.
            // Siparişler (Orders) tarihsel veri olduğu için soft-delete'e dahil edilmemişti,
            // bu yüzden sadece products, restaurants vb. tabloları temizliyoruz.
            int deletedProducts = jdbcTemplate.update("DELETE FROM products WHERE deleted_at < ?", thresholdDate);
            int deletedRestaurants = jdbcTemplate.update("DELETE FROM restaurants WHERE deleted_at < ?", thresholdDate);

            String message = String.format("Cleanup completed. Deleted %d products, %d restaurants.",
                    deletedProducts, deletedRestaurants);

            log.info(message);

            // Başarılı temizlik operasyonunu MongoDB'ye logluyoruz
            loggingService.logAction(
                    "INFO",
                    "CRON_HARD_DELETE",
                    null,
                    "SYSTEM",
                    "ALL_SOFT_DELETED",
                    message,
                    null
            );

        } catch (Exception e) {
            log.error("Error during hard delete cleanup", e);
            loggingService.logAction(
                    "ERROR", "CRON_HARD_DELETE_FAILED", null, "SYSTEM", "ALL", e.getMessage(), null
            );
            throw e; // Transaction'ın geri alınması (rollback) için fırlatıyoruz
        }
    }
}