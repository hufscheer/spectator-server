package com.sports.server.command.push.infra;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// 프로젝트의 AOP 가 빈을 CGLIB 로 감싸므로 record(final) 가 아닌 클래스로 둔다
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@ConfigurationProperties(prefix = "push.fcm")
public class FcmProperties {

    private boolean enabled;
    private String projectId;
    private String credentialsJson;
    private String webBaseUrl;

    public boolean isConfigured() {
        return enabled && hasText(projectId) && hasText(credentialsJson);
    }

    private static boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
