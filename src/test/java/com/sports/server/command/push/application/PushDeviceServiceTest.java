package com.sports.server.command.push.application;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.sports.server.command.push.domain.PushDevice;
import com.sports.server.command.push.domain.PushDeviceRepository;
import com.sports.server.command.push.domain.PushFollowRepository;
import com.sports.server.command.push.domain.PushPlatform;
import com.sports.server.command.push.dto.PushDeviceRegisterRequest;
import com.sports.server.command.push.dto.PushFollowsRequest;
import com.sports.server.command.push.dto.PushFollowsResponse;
import com.sports.server.command.push.exception.PushErrorMessages;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.NotFoundException;
import com.sports.server.support.ServiceTest;
import java.util.List;
import java.util.UUID;
import java.util.stream.LongStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.jdbc.Sql;

@Sql(scripts = "/push-notification-fixture.sql")
class PushDeviceServiceTest extends ServiceTest {

    @Autowired
    private PushDeviceService pushDeviceService;

    @Autowired
    private PushDeviceRepository pushDeviceRepository;

    @Autowired
    private PushFollowRepository pushFollowRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @DisplayName("기기를 등록할 때")
    @Nested
    class Register {

        @Test
        void 새_토큰이면_기기를_만들고_키를_돌려준다() {
            // when
            String deviceKey = register("token-1", PushPlatform.IOS);

            // then
            PushDevice device = pushDeviceRepository.findByDeviceKey(deviceKey).orElseThrow();
            assertThat(device.getToken()).isEqualTo("token-1");
            assertThat(device.getPlatform()).isEqualTo(PushPlatform.IOS);
        }

        @Test
        void 같은_토큰이면_같은_기기의_플랫폼과_접속_시각만_갱신한다() {
            // given
            String firstKey = register("token-1", PushPlatform.WEB);
            var firstSeenAt = pushDeviceRepository.findByDeviceKey(firstKey).orElseThrow().getLastSeenAt();

            // when
            String secondKey = register("token-1", PushPlatform.ANDROID);

            // then
            PushDevice device = pushDeviceRepository.findByDeviceKey(secondKey).orElseThrow();
            assertThat(secondKey).isEqualTo(firstKey);
            assertThat(device.getPlatform()).isEqualTo(PushPlatform.ANDROID);
            assertThat(device.getLastSeenAt()).isAfterOrEqualTo(firstSeenAt);
        }

        @Test
        void 대소문자만_다른_토큰은_다른_기기다() {
            // when
            String lower = register("abcdef", PushPlatform.IOS);
            String upper = register("ABCDEF", PushPlatform.IOS);

            // then
            assertThat(lower).isNotEqualTo(upper);
        }

        @Test
        void 같은_클라이언트가_짧은_시간에_많이_등록하면_막는다() {
            // given
            for (int i = 0; i < 20; i++) {
                register("busy-client", "token-" + i, PushPlatform.WEB);
            }

            // when & then
            assertThatThrownBy(() -> register("busy-client", "token-extra", PushPlatform.WEB))
                    .hasMessage(PushErrorMessages.DEVICE_REGISTER_RATE_LIMIT_EXCEEDED);
        }
    }

    @DisplayName("토큰을 갱신할 때")
    @Nested
    class RefreshToken {

        @Test
        void 기기의_토큰을_바꾼다() {
            // given
            String deviceKey = register("old-token", PushPlatform.IOS);

            // when
            pushDeviceService.refreshToken(deviceKey, "new-token");

            // then
            PushDevice device = pushDeviceRepository.findByDeviceKey(deviceKey).orElseThrow();
            assertThat(device.getToken()).isEqualTo("new-token");
            assertThat(pushDeviceRepository.findByTokenHash(PushDevice.hashOf("old-token"))).isEmpty();
        }

        @Test
        void 새_토큰을_이미_다른_기기가_쥐고_있으면_그_기기를_지운다() {
            // given
            String deviceKey = register("old-token", PushPlatform.IOS);
            String staleKey = register("new-token", PushPlatform.IOS);

            // when
            pushDeviceService.refreshToken(deviceKey, "new-token");

            // then
            assertThat(pushDeviceRepository.findByDeviceKey(staleKey)).isEmpty();
            assertThat(pushDeviceRepository.findByDeviceKey(deviceKey).orElseThrow().getToken())
                    .isEqualTo("new-token");
        }

        @Test
        void 없는_기기면_404() {
            assertThatThrownBy(() -> pushDeviceService.refreshToken("unknown", "token"))
                    .isInstanceOf(NotFoundException.class)
                    .hasMessage(PushErrorMessages.DEVICE_NOT_FOUND);
        }
    }

    @DisplayName("팔로우를 바꿀 때")
    @Nested
    class ReplaceFollows {

        @Test
        void 전체를_통째로_교체한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L, 2L), List.of(1L)));

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(2L, 3L), List.of(2L, 3L)));

            // then
            PushFollowsResponse follows = pushDeviceService.findFollows(deviceKey);
            assertThat(follows.teamIds()).containsExactly(2L, 3L);
            assertThat(follows.gameIds()).containsExactly(2L, 3L);
        }

        @Test
        void 중복된_아이디는_하나로_본다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L, 1L), List.of()));

            // then
            assertThat(pushDeviceService.findFollows(deviceKey).teamIds()).containsExactly(1L);
        }

        @Test
        void 빈_목록으로_모두_해제한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of(1L)));

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of()));

            // then
            PushFollowsResponse follows = pushDeviceService.findFollows(deviceKey);
            assertThat(follows.teamIds()).isEmpty();
            assertThat(follows.gameIds()).isEmpty();
        }

        @Test
        void 팀이_31개면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            List<Long> teamIds = LongStream.rangeClosed(1, 31).boxed().toList();

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(teamIds, List.of())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.TEAM_FOLLOW_LIMIT_EXCEEDED);
        }

        @Test
        void 경기가_31개면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            List<Long> gameIds = LongStream.rangeClosed(1, 31).boxed().toList();

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), gameIds)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.GAME_FOLLOW_LIMIT_EXCEEDED);
        }

        @Test
        void 없는_팀이면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(999L), List.of())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_TEAM_NOT_FOUND);
        }

        @Test
        void 소프트_삭제된_팀이면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 1");

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_TEAM_NOT_FOUND);
        }

        @Test
        void 없는_경기면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of(999L))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_GAME_NOT_FOUND);
        }

        @Test
        void 팔로우한_뒤_삭제된_팀은_조회에서_빠지고_교체할_때_조용히_버린다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of()));
            jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 1");
            assertThat(pushDeviceService.findFollows(deviceKey).teamIds()).isEmpty();

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L, 2L), List.of()));

            // then
            assertThat(pushDeviceService.findFollows(deviceKey).teamIds()).containsExactly(2L);
            Long deviceId = pushDeviceRepository.findByDeviceKey(deviceKey).orElseThrow().getId();
            assertThat(pushFollowRepository.findAllByDeviceId(deviceId)).hasSize(1);
        }

        @Test
        void 팔로우한_적_없는_삭제된_팀은_다른_팀과_함께여도_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            jdbcTemplate.update("UPDATE teams SET is_deleted = 1 WHERE id = 1");

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L, 2L), List.of())))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_TEAM_NOT_FOUND);
        }

        @Test
        void 팔로우한_뒤_삭제된_경기는_조회에서_빠지고_교체할_때_조용히_버린다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of(1L)));
            jdbcTemplate.update("DELETE FROM games WHERE id = 1");
            assertThat(pushDeviceService.findFollows(deviceKey).gameIds()).isEmpty();

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of(1L, 2L)));

            // then
            assertThat(pushDeviceService.findFollows(deviceKey).gameIds()).containsExactly(2L);
        }

        @Test
        void 팔로우한_적_없는_삭제된_경기는_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            jdbcTemplate.update("DELETE FROM games WHERE id = 1");

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of(1L, 2L))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_GAME_NOT_FOUND);
        }

        @Test
        void 학교도_함께_저장하고_조회한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of(1L), List.of(1L)));

            // then
            PushFollowsResponse follows = pushDeviceService.findFollows(deviceKey);
            assertThat(follows.teamIds()).containsExactly(1L);
            assertThat(follows.gameIds()).containsExactly(1L);
            assertThat(follows.organizationIds()).containsExactly(1L);
        }

        @Test
        void 학교_목록이_null이면_학교_팔로우_없이_교체한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(), List.of(), List.of(1L)));

            // when
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of(), null));

            // then
            PushFollowsResponse follows = pushDeviceService.findFollows(deviceKey);
            assertThat(follows.teamIds()).containsExactly(1L);
            assertThat(follows.organizationIds()).isEmpty();
        }

        @Test
        void 학교가_6개면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            List<Long> organizationIds = LongStream.rangeClosed(1, 6).boxed().toList();

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey,
                    new PushFollowsRequest(List.of(), List.of(), organizationIds)))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.ORGANIZATION_FOLLOW_LIMIT_EXCEEDED);
        }

        @Test
        void 없는_학교면_거부한다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);

            // when & then
            assertThatThrownBy(() -> pushDeviceService.replaceFollows(deviceKey,
                    new PushFollowsRequest(List.of(), List.of(), List.of(999L))))
                    .isInstanceOf(BadRequestException.class)
                    .hasMessage(PushErrorMessages.FOLLOW_ORGANIZATION_NOT_FOUND);
        }

        @Test
        void 없는_기기면_404() {
            assertThatThrownBy(() -> pushDeviceService.replaceFollows("unknown", new PushFollowsRequest(List.of(), List.of())))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    @DisplayName("기기를 해제할 때")
    @Nested
    class Unregister {

        @Test
        void 기기와_팔로우를_함께_지운다() {
            // given
            String deviceKey = register("token-1", PushPlatform.IOS);
            pushDeviceService.replaceFollows(deviceKey, new PushFollowsRequest(List.of(1L), List.of(1L)));
            Long deviceId = pushDeviceRepository.findByDeviceKey(deviceKey).orElseThrow().getId();

            // when
            pushDeviceService.unregister(deviceKey);

            // then
            assertThat(pushDeviceRepository.findByDeviceKey(deviceKey)).isEmpty();
            assertThat(pushFollowRepository.findAllByDeviceId(deviceId)).isEmpty();
        }

        @Test
        void 없는_기기면_404() {
            assertThatThrownBy(() -> pushDeviceService.unregister("unknown"))
                    .isInstanceOf(NotFoundException.class);
        }
    }

    // 등록 제한은 클라이언트 단위로 세므로 테스트마다 다른 클라이언트로 등록한다
    private String register(String token, PushPlatform platform) {
        return register(UUID.randomUUID().toString(), token, platform);
    }

    private String register(String clientId, String token, PushPlatform platform) {
        return pushDeviceService.register(clientId, new PushDeviceRegisterRequest(token, platform)).deviceKey();
    }
}
