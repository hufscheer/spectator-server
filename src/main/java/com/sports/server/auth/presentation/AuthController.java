package com.sports.server.auth.presentation;

import com.sports.server.auth.application.AuthService;
import com.sports.server.auth.details.MemberDetails;
import com.sports.server.auth.dto.JwtResponse;
import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.utils.CookieUtil;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    @Value("${cookie.valid-time}")
    private long COOKIE_VALID_TIME;

    @Value("${cookie.name}")
    private String COOKIE_NAME;

    private final AuthService authService;

    @PostMapping("/manager/login")
    public void managerLogin(@RequestBody LoginRequest loginRequest, HttpServletResponse response) {
        JwtResponse jwtResponse = authService.managerLogin(loginRequest);
        ResponseCookie cookie = CookieUtil.createCookie(COOKIE_NAME, jwtResponse.accessToken(), COOKIE_VALID_TIME);
        response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
    }

    @PostMapping("/register")
    public void register() {
        authService.save("sualng123@gmail.com");
    }

    @GetMapping("/manager/test")
    public String test(@AuthenticationPrincipal MemberDetails memberDetails) {
        return memberDetails.getUsername();
    }
}
