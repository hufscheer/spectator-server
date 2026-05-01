package com.sports.server.command.cheertalk.infra;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CachingMaskingClientTest {

    private OpenRouterMaskingClient delegate;
    private CachingMaskingClient client;

    @BeforeEach
    void setUp() {
        delegate = mock(OpenRouterMaskingClient.class);
        client = new CachingMaskingClient(delegate, new MaskingPreFilter(), 5L, 100L);
    }

    @Test
    @DisplayName("preFilter가 통과시키는 메시지는 delegate를 호출하지 않고 원문을 반환한다")
    void preFilter_통과시_delegate_미호출() {
        String result = client.mask("ㄱㄱ");

        assertThat(result).isEqualTo("ㄱㄱ");
        verify(delegate, never()).mask(any());
    }

    @Test
    @DisplayName("같은 메시지로 반복 호출하면 delegate는 한 번만 호출되고 캐시 결과를 반환한다")
    void 동일_메시지_캐시_적중() {
        when(delegate.mask("씨발 잘한다")).thenReturn("** 잘한다");

        String first = client.mask("씨발 잘한다");
        String second = client.mask("씨발 잘한다");

        assertThat(first).isEqualTo("** 잘한다");
        assertThat(second).isEqualTo("** 잘한다");
        verify(delegate, times(1)).mask("씨발 잘한다");
    }

    @Test
    @DisplayName("앞뒤 공백만 다른 메시지는 같은 키로 캐시 적중한다")
    void 공백_차이_캐시_적중() {
        when(delegate.mask("씨발 잘한다")).thenReturn("** 잘한다");

        client.mask("씨발 잘한다");
        client.mask("  씨발 잘한다  ");

        verify(delegate, times(1)).mask(any());
    }

    @Test
    @DisplayName("delegate가 null(=일시 오류)을 반환하면 원문을 그대로 반환하되 캐시에는 저장하지 않는다")
    void null_결과는_원문_반환_및_캐시_미저장() {
        when(delegate.mask("일시오류")).thenReturn(null);

        String first = client.mask("일시오류");
        String second = client.mask("일시오류");

        assertThat(first).isEqualTo("일시오류");
        assertThat(second).isEqualTo("일시오류");
        verify(delegate, times(2)).mask("일시오류");
    }

    @Test
    @DisplayName("null 입력은 delegate 호출 없이 null을 반환한다")
    void null_입력() {
        String result = client.mask(null);

        assertThat(result).isNull();
        verify(delegate, never()).mask(any());
    }
}
