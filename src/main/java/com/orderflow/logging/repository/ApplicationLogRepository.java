package com.orderflow.logging.repository;

import com.orderflow.logging.document.ApplicationLog;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface ApplicationLogRepository extends MongoRepository<ApplicationLog, String> {
}