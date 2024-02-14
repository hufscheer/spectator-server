package com.sports.server.support;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.restdocs.mockmvc.MockMvcRestDocumentation;
import org.springframework.restdocs.mockmvc.RestDocumentationResultHandler;
import org.springframework.restdocs.operation.preprocess.OperationRequestPreprocessor;

import static org.springframework.restdocs.operation.preprocess.Preprocessors.*;

@TestConfiguration
public class RestDocsConfig {

    private static final OperationRequestPreprocessor HOST_INFO = preprocessRequest(modifyUris()
            .scheme("https" )
            .host("www.api.hufstreaming.site" )
            .removePort(), prettyPrint()
    );

    @Bean
    public RestDocumentationResultHandler restDocsMockMvcConfigurationCustomizer() {
        return MockMvcRestDocumentation.document(
                "{class-name}/{method-name}",
                HOST_INFO,
                preprocessResponse(prettyPrint())
        );
    }
}
