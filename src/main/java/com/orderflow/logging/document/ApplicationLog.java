package com.orderflow.logging.document;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.Instant;
import java.time.OffsetDateTime;

@Document(collection = "application_logs")
@Getter
@Setter
@Builder
public class ApplicationLog {

    @Id
    private String id; // MongoDB varsayılan olarak String tipinde ObjectId üretir

    private String level; // INFO, ERROR, WARN
    private String action; // CREATE_ORDER, REGISTER, vb.
    private Long userId; // İşlemi yapan kullanıcı
    private String entityType; // ORDER, PRODUCT, CUSTOMER
    private String entityId;
    private String message;
    private Object metadata; // Hata detayı veya ekstra JSON verisi
    private Instant timestamp;
}