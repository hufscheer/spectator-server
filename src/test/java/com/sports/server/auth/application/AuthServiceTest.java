package com.sports.server.auth.application;

import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

import com.sports.server.auth.dto.LoginRequest;
import com.sports.server.auth.exception.AuthorizationErrorMessages;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.common.exception.UnauthorizedException;
import com.sports.server.support.ServiceTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/member-fixture.sql")
public class AuthServiceTest extends ServiceTest {

    @Autowired
    private AuthService authService;

    @Test
    void 존재하지_않는_사용자로_로그인을_시도할_때_예외가_발생한다() {
        // given
        String email = "notexist@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when & then
        assertThatThrownBy(
                () -> authService.managerLogin(loginRequest)
        ).isInstanceOf(NotFoundException.class)
                .hasMessage(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

    }

    @Test
    void 잘못된_비밀번호로_로그인을_시도할_때_예외가_발샐한다() {
        // given
        String email = "john@example.com";
        String password = "notvalidpassword";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when & then
        assertThatThrownBy(
                () -> authService.managerLogin(loginRequest)
        ).isInstanceOf(NotFoundException.class)
                .hasMessage(AuthorizationErrorMessages.MEMBER_NOT_FOUND_EXCEPTION)
                .extracting("status").isEqualTo(HttpStatus.NOT_FOUND);

    }


    @Test
    void 매니저가_아닌_경우_로그인을_시도할_때_예외가_발생한다() {

        // given
        String email = "jane@example.com";
        String password = "1234";
        LoginRequest loginRequest = new LoginRequest(email, password);

        // when & then
        assertThatThrownBy(
                () -> authService.managerLogin(loginRequest)
        ).isInstanceOf(UnauthorizedException.class)
                .hasMessage(AuthorizationErrorMessages.PERMISSION_DENIED)
                .extracting("status").isEqualTo(HttpStatus.UNAUTHORIZED);

    }

}
