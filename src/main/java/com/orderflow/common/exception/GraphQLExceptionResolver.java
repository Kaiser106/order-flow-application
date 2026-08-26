package com.orderflow.common.exception;

import graphql.GraphQLError;
import graphql.GraphqlErrorBuilder;
import graphql.schema.DataFetchingEnvironment;
import org.springframework.graphql.execution.DataFetcherExceptionResolverAdapter;
import org.springframework.stereotype.Component;

@Component
public class GraphQLExceptionResolver extends DataFetcherExceptionResolverAdapter {


    @Override
    protected GraphQLError resolveToSingleError(Throwable ex, DataFetchingEnvironment env) {
        if (ex instanceof BusinessException businessException) {
            // İstemciye Java yığın izi (Stack Trace) yerine kendi belirlediğimiz temiz hata kodunu dönüyoruz.
            return GraphqlErrorBuilder.newError()
                    .message(businessException.getMessage())
                    .path(env.getExecutionStepInfo().getPath())
                    .errorType(graphql.ErrorType.ValidationError)

                    .extensions(java.util.Map.of("errorCode", businessException.getErrorCode()))
                    .build();
        }

        return super.resolveToSingleError(ex, env);
    }
}