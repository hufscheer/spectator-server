package com.sports.server.query.auth;

import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final int maxAge = 60 * 60 * 24 * 7;
    private final String nameOfCookie = "HCC_SES";

    @PostMapping("/login")
    public void login(HttpServletResponse response, final LoginVO loginVO) {
        String accessToken = authService.login(loginVO);
        addCookie(response, accessToken);
    }

    private void addCookie(HttpServletResponse response, final String accessToken) {
        ResponseCookie cookie = ResponseCookie.from(nameOfCookie, accessToken)
                .path("/").sameSite("Strict").secure(true).maxAge(maxAge).httpOnly(true)
                .build();
        response.addHeader("Set-Cookie", cookie.toString());
    }
}
