package com.sports.server.common.presentation;

import com.sports.server.common.application.PresignedUrlGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/manager/aws")
@RequiredArgsConstructor
public class S3Controller {

    private final PresignedUrlGenerator presignedUrlGenerator;

    @GetMapping("/generate-presigned-url")
    public ResponseEntity<String> generatePresignedUrl(@RequestParam String extension) {
        return ResponseEntity.ok(presignedUrlGenerator.generatePresignedUrl(extension));
    }
}
