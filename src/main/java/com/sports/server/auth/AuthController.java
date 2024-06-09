package com.sports.server.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseCookie;
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
    public void managerLogin(@RequestBody LoginVO loginVO, HttpServletResponse response) {
        JwtResponse jwtResponse = authService.managerLogin(loginVO);
        ResponseCookie cookie = CookieUtil.createCookie(jwtResponse.accessToken(), COOKIE_VALID_TIME);
        response.addHeader(COOKIE_NAME, cookie.toString());
    }
}
