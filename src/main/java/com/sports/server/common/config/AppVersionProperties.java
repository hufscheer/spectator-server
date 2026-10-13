package com.sports.server.common.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

// 프로젝트의 AOP 가 빈을 CGLIB 로 감싸므로 record(final) 가 아닌 클래스로 둔다
@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.version")
public class AppVersionProperties {

    private Platform ios = new Platform();
    private Platform android = new Platform();

    @Getter
    @Setter
    @NoArgsConstructor
    public static class Platform {

        private int minimumBuild = 0;
        private String storeUrl;
    }
}
