package com.sports.server.command.team.domain;

import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.Repository;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

public interface TeamRepository extends Repository<Team, Long> {
    Team save(Team team);

    void delete(Team team);

    List<Team> findAllById(Iterable<Long> ids);

    List<Team> findAll();

    long countByIdIn(Collection<Long> ids);

    @Query("SELECT t.id FROM Team t WHERE t.id IN :ids")
    List<Long> findExistingIds(@Param("ids") Collection<Long> ids);

    /**
     * 로고 주소가 {@code from} 인 팀을 모두 {@code to} 로 바꾼다. 지운(소프트 삭제) 팀도 바꾼다:
     * 원본은 바꾼 뒤 지우므로, 되살린 팀이 없는 주소를 가리키지 않게 한다.
     * 주소가 같을 때만 바꾸므로 그 사이 사용자가 다른 로고로 바꿨으면 0 을 돌려준다.
     *
     * <p>커밋 뒤(AFTER_COMMIT) 리스너에서 부른다. 그 자리에서는 끝난 트랜잭션에 묶여 있을 수 있어 새 트랜잭션을 연다.
     */
    @Modifying(clearAutomatically = true)
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    @Query(value = "UPDATE teams SET logo_image_url = :to WHERE logo_image_url = :from", nativeQuery = true)
    int replaceLogoImageUrl(@Param("from") String from, @Param("to") String to);
}
