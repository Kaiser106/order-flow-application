package com.orderflow.common.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.security.access.AccessDeniedException; // Spring Security Importu
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Map;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {

    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        // 1. Varsayılan (Bilinmeyen Hata) Şablonu
        String errorCode = "ERR_SYSTEM";
        String errorMessage = ex.getMessage() != null ? ex.getMessage() : "Beklenmeyen bir sistem hatası oluştu.";
        String actionableSolution = "Çözüm: Lütfen işleminizi kontrol edip daha sonra tekrar deneyin veya sistem yöneticisine başvurun.";

        // 2. Fırlatılan hatanın türüne göre Mesajı, Kodu ve Çözümü Akıllıca Değiştiriyoruz
        if (ex instanceof BusinessException businessException) {
            errorCode = businessException.getErrorCode();
            actionableSolution = getActionableSolution(errorCode);
        }
        else if (ex instanceof UnauthorizedException) {
            errorCode = "ERR_UNAUTHORIZED";
            actionableSolution = "Çözüm: Oturumunuzun süresi dolmuş veya sisteme giriş yapmamışsınız. Lütfen '/api/auth/login' üzerinden tekrar giriş yapın.";
        }
        else if (ex instanceof ForbiddenException || ex instanceof AccessDeniedException) {
            errorCode = "ERR_FORBIDDEN";
            errorMessage = "Bu işlemi gerçekleştirmek için yetkiniz bulunmuyor.";
            actionableSolution = "Çözüm: Erişmeye çalıştığınız veriler sizin rolünüze (Müşteri / Kurye / Restoran) veya hesabınıza ait değil. Doğru rol ile giriş yaptığınızdan emin olun.";
        }
        else if (ex instanceof IllegalArgumentException) {
            errorCode = "ERR_BAD_REQUEST";
            actionableSolution = "Çözüm: Gönderilen parametreler (örneğin ID formatı veya boş bırakılan alanlar) geçersiz. Lütfen girdiğiniz verileri kontrol edin.";
        }

        // 3. Ortaya çıkan bu verileri temiz bir GraphQL JSON'ı halinde istemciye yolluyoruz
        return GraphqlErrorBuilder.newError()
                .message(errorMessage)
                .path(env.getExecutionStepInfo().getPath())
                .errorType(graphql.ErrorType.DataFetchingException)
                .extensions(Map.of(
                        "errorCode", errorCode,
                        "solution", actionableSolution,
                        "timestamp", Instant.now().toString()
                ))
                .build();
    }

    /**
     * İş kuralları (Business Rule) hatalarını yönettiğimiz sözlük
     */
    private String getActionableSolution(String errorCode) {
        return switch (errorCode) {
            case "ERR_REST_01" -> "Çözüm: Seçtiğiniz restoran şu anda kapalı veya sistemde pasif durumda. Lütfen açık olan farklı bir restoran seçin.";
            case "ERR_PROD_01" -> "Çözüm: Sepetinizdeki ürünlerden biri artık menüde yok veya tükenmiş. Lütfen ürünü sepetten çıkarıp menüden güncel bir ürün ekleyin.";
            case "ERR_ORD_02"  -> "Çözüm: Sipariş hazırlık aşamasını geçtiği için sistem üzerinden iptal edilemez. Acil durumlar için doğrudan restoranı aramalısınız.";
            case "ERR_ORD_03"  -> "Çözüm: Bu sipariş havuzdan saniyeler önce başka bir kurye tarafından alındı. Sayfayı yenileyip havuzdaki diğer siparişlere yönelin.";
            case "ERR_JSON_PARSE" -> "Çözüm: Adres veya çalışma saatleri verisinde tırnak (\") veya parantez hatası var. Geçerli bir JSON string formatı gönderdiğinizden emin olun.";
            default -> "Çözüm: İşleminizi kontrol edip daha sonra tekrar deneyin veya destek ekibiyle iletişime geçin.";
        };
    }
}