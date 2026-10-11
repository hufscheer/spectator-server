package com.sports.server.command.push.presentation;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.delete;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.get;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.post;
import static org.springframework.restdocs.mockmvc.RestDocumentationRequestBuilders.put;
import static org.springframework.restdocs.payload.PayloadDocumentation.fieldWithPath;
import static org.springframework.restdocs.payload.PayloadDocumentation.requestFields;
import static org.springframework.restdocs.payload.PayloadDocumentation.responseFields;
import static org.springframework.restdocs.request.RequestDocumentation.parameterWithName;
import static org.springframework.restdocs.request.RequestDocumentation.pathParameters;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.sports.server.command.push.domain.PushPlatform;
import com.sports.server.command.push.dto.PushDeviceRegisterRequest;
import com.sports.server.command.push.dto.PushDeviceResponse;
import com.sports.server.command.push.dto.PushFollowsRequest;
import com.sports.server.command.push.dto.PushFollowsResponse;
import com.sports.server.command.push.dto.PushTokenRefreshRequest;
import com.sports.server.command.push.exception.PushErrorMessages;
import com.sports.server.command.push.exception.PushRateLimitException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.DocumentationTest;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.http.MediaType;
import org.springframework.restdocs.payload.JsonFieldType;
import org.springframework.test.web.servlet.ResultActions;

public class PushDeviceControllerTest extends DocumentationTest {

    private static final String DEVICE_KEY = "6f1c2c7e-5f1c-4f58-8a8e-6a0c3b7f9a10";

    @Test
    void 푸시_기기를_등록한다() throws Exception {
        // given
        PushDeviceRegisterRequest request = new PushDeviceRegisterRequest("fcm-token", PushPlatform.IOS);
        given(pushDeviceService.register(any(), eq(request))).willReturn(new PushDeviceResponse(DEVICE_KEY));

        // when
        ResultActions result = mockMvc.perform(post("/push/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        requestFields(
                                fieldWithPath("token").type(JsonFieldType.STRING).description("FCM 등록 토큰"),
                                fieldWithPath("platform").type(JsonFieldType.STRING)
                                        .description("기기 종류 (IOS, ANDROID, WEB)")
                        ),
                        responseFields(
                                fieldWithPath("deviceKey").type(JsonFieldType.STRING)
                                        .description("이후 요청에 쓰는 기기 비밀 키. 클라이언트가 보관한다")
                        )
                ));
    }

    @Test
    void 토큰이_비어_있으면_등록할_수_없다() throws Exception {
        // given
        PushDeviceRegisterRequest request = new PushDeviceRegisterRequest(" ", PushPlatform.WEB);

        // when
        ResultActions result = mockMvc.perform(post("/push/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 알_수_없는_플랫폼이면_등록할_수_없다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(post("/push/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"token\": \"fcm-token\", \"platform\": \"WINDOWS\"}"));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 등록_요청이_너무_많으면_429를_돌려준다() throws Exception {
        // given
        PushDeviceRegisterRequest request = new PushDeviceRegisterRequest("fcm-token", PushPlatform.WEB);
        given(pushDeviceService.register(any(), eq(request)))
                .willThrow(new PushRateLimitException(PushErrorMessages.DEVICE_REGISTER_RATE_LIMIT_EXCEEDED));

        // when
        ResultActions result = mockMvc.perform(post("/push/devices")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isTooManyRequests());
    }

    @Test
    void 푸시_토큰을_갱신한다() throws Exception {
        // given
        PushTokenRefreshRequest request = new PushTokenRefreshRequest("new-fcm-token");

        // when
        ResultActions result = mockMvc.perform(put("/push/devices/{deviceKey}/token", DEVICE_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("deviceKey").description("기기 비밀 키")),
                        requestFields(
                                fieldWithPath("token").type(JsonFieldType.STRING).description("새 FCM 등록 토큰")
                        )
                ));
    }

    @Test
    void 없는_기기의_토큰을_갱신하면_404를_돌려준다() throws Exception {
        // given
        org.mockito.Mockito.doThrow(new NotFoundException(PushErrorMessages.DEVICE_NOT_FOUND))
                .when(pushDeviceService).refreshToken(eq("unknown"), any());

        // when
        ResultActions result = mockMvc.perform(put("/push/devices/{deviceKey}/token", "unknown")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(new PushTokenRefreshRequest("new-fcm-token"))));

        // then
        result.andExpect(status().isNotFound());
    }

    @Test
    void 팔로우_목록을_조회한다() throws Exception {
        // given
        given(pushDeviceService.findFollows(DEVICE_KEY))
                .willReturn(new PushFollowsResponse(List.of(1L, 2L), List.of(10L)));

        // when
        ResultActions result = mockMvc.perform(get("/push/devices/{deviceKey}/follows", DEVICE_KEY));

        // then
        result.andExpect(status().isOk())
                .andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("deviceKey").description("기기 비밀 키")),
                        responseFields(
                                fieldWithPath("teamIds").type(JsonFieldType.ARRAY).description("팔로우 중인 팀 ID"),
                                fieldWithPath("gameIds").type(JsonFieldType.ARRAY).description("팔로우 중인 경기 ID")
                        )
                ));
    }

    @Test
    void 팔로우_목록을_통째로_교체한다() throws Exception {
        // given
        PushFollowsRequest request = new PushFollowsRequest(List.of(1L, 2L), List.of(10L));

        // when
        ResultActions result = mockMvc.perform(put("/push/devices/{deviceKey}/follows", DEVICE_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request)));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("deviceKey").description("기기 비밀 키")),
                        requestFields(
                                fieldWithPath("teamIds").type(JsonFieldType.ARRAY)
                                        .description("팔로우할 팀 ID (최대 30개)"),
                                fieldWithPath("gameIds").type(JsonFieldType.ARRAY)
                                        .description("팔로우할 경기 ID (최대 30개)")
                        )
                ));
    }

    @Test
    void 팔로우_목록에_null이_섞이면_거부한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(put("/push/devices/{deviceKey}/follows", DEVICE_KEY)
                .contentType(MediaType.APPLICATION_JSON)
                .content("{\"teamIds\": [1, null], \"gameIds\": []}"));

        // then
        result.andExpect(status().isBadRequest());
    }

    @Test
    void 푸시_기기를_해제한다() throws Exception {
        // when
        ResultActions result = mockMvc.perform(delete("/push/devices/{deviceKey}", DEVICE_KEY));

        // then
        result.andExpect(status().isNoContent())
                .andDo(restDocsHandler.document(
                        pathParameters(parameterWithName("deviceKey").description("기기 비밀 키"))
                ));
    }
}
