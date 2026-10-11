package com.sports.server.command.push.presentation;

import com.sports.server.command.push.application.PushDeviceService;
import com.sports.server.command.push.dto.PushDeviceRegisterRequest;
import com.sports.server.command.push.dto.PushDeviceResponse;
import com.sports.server.command.push.dto.PushFollowsRequest;
import com.sports.server.command.push.dto.PushFollowsResponse;
import com.sports.server.command.push.dto.PushTokenRefreshRequest;
import com.sports.server.common.util.VisitorIdResolver;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/push/devices")
public class PushDeviceController {

    private final PushDeviceService pushDeviceService;

    @PostMapping
    public ResponseEntity<PushDeviceResponse> register(@RequestBody @Valid PushDeviceRegisterRequest request,
                                                       HttpServletRequest httpRequest,
                                                       HttpServletResponse httpResponse) {
        String visitorId = VisitorIdResolver.resolveOrIssue(httpRequest, httpResponse);
        return ResponseEntity.ok(pushDeviceService.register(visitorId, request));
    }

    @PutMapping("/{deviceKey}/token")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void refreshToken(@PathVariable String deviceKey, @RequestBody @Valid PushTokenRefreshRequest request) {
        pushDeviceService.refreshToken(deviceKey, request.token());
    }

    @GetMapping("/{deviceKey}/follows")
    public ResponseEntity<PushFollowsResponse> findFollows(@PathVariable String deviceKey) {
        return ResponseEntity.ok(pushDeviceService.findFollows(deviceKey));
    }

    @PutMapping("/{deviceKey}/follows")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void replaceFollows(@PathVariable String deviceKey, @RequestBody @Valid PushFollowsRequest request) {
        pushDeviceService.replaceFollows(deviceKey, request);
    }

    @DeleteMapping("/{deviceKey}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unregister(@PathVariable String deviceKey) {
        pushDeviceService.unregister(deviceKey);
    }
}
