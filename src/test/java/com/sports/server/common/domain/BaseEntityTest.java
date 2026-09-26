package com.sports.server.common.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.sports.server.command.player.domain.Player;
import com.sports.server.command.team.domain.Team;
import com.sports.server.command.timeline.domain.ScoreTimeline;
import com.sports.server.command.timeline.domain.Timeline;
import com.sports.server.support.ServiceTest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import java.util.List;
import java.util.function.Function;
import org.hibernate.Hibernate;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.jdbc.Sql;

/**
 * 지연 로딩된 연관은 Hibernate 프록시라 클래스가 다르고 필드도 비어 있다.
 * getClass() 로 비교하던 때는 프록시와 그 실제 엔티티를 다르다고 봐서, 자책골 삭제가 엉뚱한 팀 점수를 깎았다 (#715).
 */
@Sql(scripts = "/timeline-fixture.sql")
@DisplayName("엔티티 비교는")
class BaseEntityTest extends ServiceTest {

    @Autowired
    private EntityManagerFactory entityManagerFactory;

    @Test
    void 지연_로딩_프록시와_같은_엔티티면_같다고_본다() {
        // given
        Team team = inNewEntityManager(em -> em.find(Team.class, 1L));

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Team proxy = em.getReference(Team.class, 1L);
            assertThat(Hibernate.isInitialized(proxy)).isFalse();

            // when & then
            assertThat(team.equals(proxy)).isTrue();
            assertThat(List.of(proxy).contains(team)).isTrue();
            // 비교하느라 프록시를 초기화(DB 조회)하지 않는다
            assertThat(Hibernate.isInitialized(proxy)).isFalse();
            assertThat(proxy.equals(team)).isTrue();
        } finally {
            em.close();
        }
    }

    @Test
    void 영속성_컨텍스트가_닫힌_프록시와도_예외_없이_비교한다() {
        // given
        Team team = inNewEntityManager(em -> em.find(Team.class, 1L));
        Team closedProxy = inNewEntityManager(em -> em.getReference(Team.class, 1L));

        // when & then
        assertThat(team.equals(closedProxy)).isTrue();
    }

    @Test
    void 상속_엔티티는_상위_타입_프록시와도_같다고_본다() {
        // given
        Long scoreTimelineId = inNewEntityManager(em -> em
                .createQuery("select st.id from ScoreTimeline st order by st.id", Long.class)
                .setMaxResults(1)
                .getSingleResult());
        Timeline scoreTimeline = inNewEntityManager(em -> em.find(ScoreTimeline.class, scoreTimelineId));

        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            Timeline proxy = em.getReference(Timeline.class, scoreTimelineId);

            // when & then
            assertThat(scoreTimeline.equals(proxy)).isTrue();
        } finally {
            em.close();
        }
    }

    @Test
    void 다른_엔티티는_다르다고_본다() {
        // given
        Team team = inNewEntityManager(em -> em.find(Team.class, 1L));
        Team otherTeam = inNewEntityManager(em -> em.find(Team.class, 2L));
        Player playerWithSameId = inNewEntityManager(em -> em.find(Player.class, 1L));

        // when & then
        assertThat(team.equals(otherTeam)).isFalse();
        assertThat(team.equals(playerWithSameId)).isFalse();
    }

    private <T> T inNewEntityManager(Function<EntityManager, T> work) {
        EntityManager em = entityManagerFactory.createEntityManager();
        try {
            return work.apply(em);
        } finally {
            em.close();
        }
    }
}
