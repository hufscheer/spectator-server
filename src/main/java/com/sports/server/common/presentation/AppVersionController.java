package com.sports.server.common.presentation;

import com.sports.server.common.config.AppVersionProperties;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/app")
@EnableConfigurationProperties(AppVersionProperties.class)
@RequiredArgsConstructor
public class AppVersionController {

    private final AppVersionProperties properties;

    @GetMapping("/versions")
    public ResponseEntity<AppVersionsResponse> versions() {
        return ResponseEntity.ok(new AppVersionsResponse(
                PlatformPolicy.from(properties.getIos()),
                PlatformPolicy.from(properties.getAndroid())
        ));
    }

    public record AppVersionsResponse(PlatformPolicy ios, PlatformPolicy android) {
    }

    public record PlatformPolicy(int minimumBuild, String storeUrl) {

        static PlatformPolicy from(AppVersionProperties.Platform platform) {
            return new PlatformPolicy(platform.getMinimumBuild(), platform.getStoreUrl());
        }
    }
}
