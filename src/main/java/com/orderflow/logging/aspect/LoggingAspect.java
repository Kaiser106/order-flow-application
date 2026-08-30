package com.orderflow.logging.aspect;

import com.orderflow.auth.service.CustomUserDetails;
import com.orderflow.logging.service.LoggingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

import java.util.Arrays;

/**
 * @Aspect: Spring'e bu sınıfın bir AOP bileşeni olduğunu bildirir.
 * Kurumsal Kural: İş mantığı (Business Logic) ASLA Aspect içine yazılmaz.
 * Aspect sadece metodu gözlemler, süresini ölçer veya log atar. Karar alıcı değildir.
 */
@Aspect
@Component
@Slf4j
@RequiredArgsConstructor
public class LoggingAspect {

    private final LoggingService loggingService;

    // Pointcut: Hangi metotların arasına gireceğimizi (intercept/yakaladigimiz) tanımladığımız kural.
    // "com.orderflow altındaki tüm 'service' paketlerinde bulunan tüm metotlar" anlamına gelir.
    // Pointcut kuralını güncelliyoruz: Service paketlerini dinle AMA logging paketini (ve kendini) DİNLEME!
    @Pointcut("within(com.orderflow..service..*) && !within(com.orderflow.logging..*)")
    public void serviceLayerPointcut() {}

    /**
     * @Around: Hedef metot çalışmadan HEMEN ÖNCE ve çalıştıktan HEMEN SONRA devreye girer.
     * Performans (Execution Time) ölçümü ve loglama için en ideal anotasyondur.
     */
    @Around("serviceLayerPointcut()")
    public Object logServiceAccess(ProceedingJoinPoint joinPoint) throws Throwable {
        String methodName = joinPoint.getSignature().getDeclaringTypeName() + "." + joinPoint.getSignature().getName();
        Long currentUserId = getCurrentUserIdSafely();

        // Şifre sızıntısını önlemek için argümanları filtreden geçiriyoruz
        String maskedArgs = maskSensitiveArguments(joinPoint.getArgs());

        long startTime = System.currentTimeMillis();

        try {
            // Asıl servisin metodunu çalıştırır (Örn: OrderService.createOrder)
            Object result = joinPoint.proceed();

            long executionTime = System.currentTimeMillis() - startTime;

            // Çok uzun süren metotları (Örn: 2 saniyeden uzun) WARN seviyesinde logluyoruz
            if (executionTime > 2000) {
                log.warn("PERFORMANCE_WARNING - {} executed in {} ms", methodName, executionTime);
            }

            // Genel başarılı işlem logu (MongoDB'ye)
            loggingService.logAction(
                    "INFO",
                    "METHOD_EXECUTION",
                    currentUserId,
                    "SERVICE",
                    methodName,
                    "Execution successful in " + executionTime + " ms",
                    null
            );

            return result;

        } catch (IllegalArgumentException | IllegalStateException e) {
            // Bilinen iş kuralları hataları
            log.warn("Illegal argument in {}: {} - Args: {}", methodName, e.getMessage(), maskedArgs);
            throw e;
        } catch (Exception e) {
            // Beklenmeyen sistem hatalarını ERROR olarak MongoDB'ye yazıyoruz
            loggingService.logAction(
                    "ERROR",
                    "SYSTEM_EXCEPTION",
                    currentUserId,
                    "SERVICE",
                    methodName,
                    e.getMessage(),
                    maskedArgs
            );
            log.error("Exception in {} with cause = {} - Args: {}", methodName, e.getCause() != null ? e.getCause() : "NULL", maskedArgs);
            throw e;
        }
    }

    // Güvenlik Kuralı: Auth ile ilgili metotların parametrelerini (İçinde şifre olan DTO'ları) maskele!
    private String maskSensitiveArguments(Object[] args) {
        if (args == null || args.length == 0) return "";

        return Arrays.stream(args)
                .map(arg -> {
                    if (arg != null && (arg.getClass().getSimpleName().contains("LoginRequest") ||
                            arg.getClass().getSimpleName().contains("RegisterRequest"))) {
                        return "[PROTECTED_CREDENTIALS]";
                    }
                    return arg != null ? arg.toString() : "null";
                })
                .reduce((a, b) -> a + ", " + b)
                .orElse("");
    }

    // Sistem hata vermesin diye kimlik bilgisini güvenli çeken yardımcı metot
    private Long getCurrentUserIdSafely() {
        try {
            Authentication auth = SecurityContextHolder.getContext().getAuthentication();
            if (auth != null && auth.getPrincipal() instanceof CustomUserDetails userDetails) {
                return userDetails.getUser().getId();
            }
        } catch (Exception e) {
            // Güvenlik bağlamı yoksa (Örn: Giriş yapmamış biri) id dönme
        }
        return null;
    }
}