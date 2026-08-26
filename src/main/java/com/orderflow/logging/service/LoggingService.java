package com.orderflow.logging.service;

import com.orderflow.logging.document.ApplicationLog;
import com.orderflow.logging.repository.ApplicationLogRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.Instant;

@Service
@RequiredArgsConstructor
public class LoggingService {

    private final ApplicationLogRepository applicationLogRepository;

    public void logAction(String level, String action, Long userId, String entityType, String entityId, String message, Object metadata) {
        ApplicationLog logEntry = ApplicationLog.builder()
                .level(level)
                .action(action)
                .userId(userId)
                .entityType(entityType)
                .entityId(entityId)
                .message(message)
                .metadata(metadata)
                .timestamp(Instant.now())
                .build();

        applicationLogRepository.save(logEntry);
    }
}