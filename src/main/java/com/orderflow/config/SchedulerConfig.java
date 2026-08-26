package com.orderflow.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;

@Configuration
@EnableScheduling // Spring'in arka planda zamanlanmış görevleri (Cron) taramasını sağlar
public class SchedulerConfig {
}