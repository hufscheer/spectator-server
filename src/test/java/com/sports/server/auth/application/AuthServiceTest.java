package com.sports.server.auth.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.Assert.assertEquals;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.sports.server.auth.dto.LoginResponse;
import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.auth.utils.JwtUtil;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.jdbc.Sql;

@ActiveProfiles("dev")
@Sql(scripts = "/member-fixture.sql")
public class AuthServiceTest extends ServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private JwtUtil jwtUtil;

    @Nested
    @DisplayName("로그인을 할 때 성공하는 경우")
    class LoginSuccessTest {
        @Test
        void 예외를_던지지_않는다() {
            // given
            String email = "john@example.com";
            String password = "1234";
            LoginRequest loginRequest = new LoginRequest(email, password);

            // when & then
            assertDoesNotThrow(() -> {
                authService.loginByManager(loginRequest);
            });
        }

        @Test
        void 올바른_액세스_토큰을_반환한다() {
            // given
            String email = "john@example.com";
            String password = "1234";
            LoginRequest loginRequest = new LoginRequest(email, password);

            // when & then
            LoginResponse response = authService.loginByManager(loginRequest);
            String accessToken = response.accessToken();
            assertEquals(email, jwtUtil.getEmail(accessToken));
        }
    }

    @Nested
    @DisplayName("로그인을 할 때")
    class LoginFailureTest {

        @Test
        void 존재하지_않는_사용자인_경우_예외가_발생한다() {
            // given
            String email = "notexist@example.com";
            String password = "1234";
            LoginRequest loginRequest = new LoginRequest(email, password);

            // when & then
            assertThatThrownBy(
                    () -> authService.loginByManager(loginRequest)
            ).isInstanceOf(NotFoundException.class)
                    .hasMessage(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION)
                    .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        }

        @Test
        void 잘못된_비밀번호인_경우_예외가_발샐한다() {
            // given
            String email = "john@example.com";
            String password = "invalid-password";
            LoginRequest loginRequest = new LoginRequest(email, password);

            // when & then
            assertThatThrownBy(
                    () -> authService.loginByManager(loginRequest)
            ).isInstanceOf(NotFoundException.class)
                    .hasMessage(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION)
                    .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

        }

        @Test
        void 매니저가_권한이_없는_경우_예외가_발생한다() {

            // given
            String email = "jane@example.com";
            String password = "1234";
            LoginRequest loginRequest = new LoginRequest(email, password);

            // when & then
            assertThatThrownBy(
                    () -> authService.loginByManager(loginRequest)
            ).isInstanceOf(UnauthorizedException.class)
                    .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED)
                    .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);

        }
    }

}
