package com.orderflow.logging.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.util.UUID;
import java.time.OffsetDateTime;

@Document(collection = "application_logs")
@Getter
@Setter
@Builder
public class ApplicationLog {

    @Id
    private String id;

    private String level;
    private String action;
    private UUID userId;
    private String entityType;
    private String entityId;
    private String message;
    private Object metadata;
    private Instant timestamp;
}