package com.sports.server.command.push.application;

import static com.sports.server.command.push.exception.PushErrorMessages.DEVICE_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.FOLLOW_GAME_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.FOLLOW_TEAM_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.FOLLOW_ORGANIZATION_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.GAME_FOLLOW_LIMIT_EXCEEDED;
import static com.sports.server.command.push.exception.PushErrorMessages.ORGANIZATION_FOLLOW_LIMIT_EXCEEDED;
import static com.sports.server.command.push.exception.PushErrorMessages.TEAM_FOLLOW_LIMIT_EXCEEDED;

import com.sports.server.command.game.domain.GameRepository;
import com.sports.server.command.organization.domain.OrganizationRepository;
import com.sports.server.command.push.domain.FollowTargetType;
import com.sports.server.command.push.domain.PushDevice;
import com.sports.server.command.push.domain.PushDeviceRepository;
import com.sports.server.command.push.domain.PushFollow;
import com.sports.server.command.push.domain.PushFollowRepository;
import com.sports.server.command.push.dto.PushDeviceRegisterRequest;
import com.sports.server.command.push.dto.PushDeviceResponse;
import com.sports.server.command.push.dto.PushFollowsRequest;
import com.sports.server.command.push.dto.PushFollowsResponse;
import com.sports.server.command.team.domain.TeamRepository;
import com.sports.server.common.exception.BadRequestException;
import com.sports.server.common.exception.NotFoundException;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
@Transactional
@RequiredArgsConstructor
public class PushDeviceService {

    private static final int MAX_FOLLOWS_PER_TYPE = 30;
    private static final int MAX_ORGANIZATION_FOLLOWS = 5;

    private final PushDeviceRepository pushDeviceRepository;
    private final PushFollowRepository pushFollowRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
    private final OrganizationRepository organizationRepository;
    private final PushDeviceRegisterRateLimiter registerRateLimiter;

    public PushDeviceResponse register(String clientId, PushDeviceRegisterRequest request) {
        registerRateLimiter.check(clientId);

        PushDevice device = pushDeviceRepository.findByTokenHash(PushDevice.hashOf(request.token()))
                .map(existing -> touched(existing, request))
                .orElseGet(() -> pushDeviceRepository.save(new PushDevice(request.token(), request.platform())));
        return new PushDeviceResponse(device.getDeviceKey());
    }

    private PushDevice touched(PushDevice device, PushDeviceRegisterRequest request) {
        device.touch(request.platform());
        return device;
    }

    // 같은 토큰은 한 설치본뿐이라, 다른 행이 쥐고 있던 토큰은 낡은 기록으로 보고 치운다
    public void refreshToken(String deviceKey, String token) {
        PushDevice device = getDevice(deviceKey);
        pushDeviceRepository.deleteOtherOwnersOfToken(PushDevice.hashOf(token), device.getId());
        device.changeToken(token);
    }

    @Transactional(readOnly = true)
    public PushFollowsResponse findFollows(String deviceKey) {
        PushDevice device = getDevice(deviceKey);
        List<PushFollow> follows = pushFollowRepository.findAllByDeviceId(device.getId());
        // 팔로우한 대상이 나중에 지워질 수 있어, 남아 있는 것만 돌려준다
        return new PushFollowsResponse(
                existingOf(targetIdsOf(follows, FollowTargetType.TEAM), teamRepository::findExistingIds),
                existingOf(targetIdsOf(follows, FollowTargetType.GAME), gameRepository::findExistingIds),
                existingOf(targetIdsOf(follows, FollowTargetType.ORGANIZATION), organizationRepository::findExistingIds)
        );
    }

    private List<Long> existingOf(List<Long> ids, Function<Collection<Long>, List<Long>> finder) {
        if (ids.isEmpty()) {
            return List.of();
        }
        Set<Long> existing = new HashSet<>(finder.apply(ids));
        return ids.stream().filter(existing::contains).toList();
    }

    private List<Long> targetIdsOf(List<PushFollow> follows, FollowTargetType type) {
        return follows.stream()
                .filter(follow -> follow.getTargetType() == type)
                .map(PushFollow::getTargetId)
                .sorted(Comparator.naturalOrder())
                .toList();
    }

    public void replaceFollows(String deviceKey, PushFollowsRequest request) {
        PushDevice device = getDevice(deviceKey);
        Set<Long> teamIds = new LinkedHashSet<>(request.teamIds());
        Set<Long> gameIds = new LinkedHashSet<>(request.gameIds());
        Set<Long> organizationIds = new LinkedHashSet<>(request.organizationIds());
        validateLimits(teamIds, gameIds, organizationIds);

        // 팔로우한 뒤 지워진 대상은 조용히 빼고 저장한다. 낡은 아이디 때문에 기기가 영영 잠기면 안 된다.
        // 팔로우한 적도 없고 존재하지도 않는 아이디는 그대로 400 이다
        List<PushFollow> current = pushFollowRepository.findAllByDeviceId(device.getId());
        Set<Long> existingTeamIds = keepExisting(teamIds, followedIdsOf(current, FollowTargetType.TEAM),
                teamRepository::findExistingIds, FOLLOW_TEAM_NOT_FOUND);
        Set<Long> existingGameIds = keepExisting(gameIds, followedIdsOf(current, FollowTargetType.GAME),
                gameRepository::findExistingIds, FOLLOW_GAME_NOT_FOUND);
        Set<Long> existingOrganizationIds = keepExisting(organizationIds,
                followedIdsOf(current, FollowTargetType.ORGANIZATION),
                organizationRepository::findExistingIds, FOLLOW_ORGANIZATION_NOT_FOUND);

        pushFollowRepository.deleteAllByDeviceId(device.getId());
        pushFollowRepository.saveAll(
                followsOf(device, existingTeamIds, existingGameIds, existingOrganizationIds));
    }

    private Set<Long> followedIdsOf(List<PushFollow> follows, FollowTargetType type) {
        return follows.stream()
                .filter(follow -> follow.getTargetType() == type)
                .map(PushFollow::getTargetId)
                .collect(Collectors.toSet());
    }

    private Set<Long> keepExisting(Set<Long> requested, Set<Long> followed,
                                   Function<Collection<Long>, List<Long>> finder, String notFoundMessage) {
        if (requested.isEmpty()) {
            return requested;
        }
        Set<Long> existing = new HashSet<>(finder.apply(requested));
        boolean unknownNewTarget = requested.stream()
                .anyMatch(id -> !existing.contains(id) && !followed.contains(id));
        if (unknownNewTarget) {
            throw new BadRequestException(notFoundMessage);
        }
        return requested.stream()
                .filter(existing::contains)
                .collect(Collectors.toCollection(LinkedHashSet::new));
    }

    private void validateLimits(Set<Long> teamIds, Set<Long> gameIds, Set<Long> organizationIds) {
        if (teamIds.size() > MAX_FOLLOWS_PER_TYPE) {
            throw new BadRequestException(TEAM_FOLLOW_LIMIT_EXCEEDED);
        }
        if (gameIds.size() > MAX_FOLLOWS_PER_TYPE) {
            throw new BadRequestException(GAME_FOLLOW_LIMIT_EXCEEDED);
        }
        if (organizationIds.size() > MAX_ORGANIZATION_FOLLOWS) {
            throw new BadRequestException(ORGANIZATION_FOLLOW_LIMIT_EXCEEDED);
        }
    }

    private List<PushFollow> followsOf(PushDevice device, Collection<Long> teamIds, Collection<Long> gameIds,
                                      Collection<Long> organizationIds) {
        Stream<PushFollow> teams = teamIds.stream()
                .map(id -> new PushFollow(device.getId(), FollowTargetType.TEAM, id));
        Stream<PushFollow> games = gameIds.stream()
                .map(id -> new PushFollow(device.getId(), FollowTargetType.GAME, id));
        Stream<PushFollow> organizations = organizationIds.stream()
                .map(id -> new PushFollow(device.getId(), FollowTargetType.ORGANIZATION, id));
        return Stream.of(teams, games, organizations).flatMap(stream -> stream).toList();
    }

    public void unregister(String deviceKey) {
        remove(getDevice(deviceKey));
    }

    // 커밋 직후 콜백에서 불리면 이미 끝난 트랜잭션에 얹히므로 새 트랜잭션으로 연다
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void removeAll(Collection<Long> deviceIds) {
        pushDeviceRepository.findAllById(deviceIds).forEach(this::remove);
    }

    private void remove(PushDevice device) {
        pushFollowRepository.deleteAllByDeviceId(device.getId());
        pushDeviceRepository.delete(device);
    }

    private PushDevice getDevice(String deviceKey) {
        return pushDeviceRepository.findByDeviceKey(deviceKey)
                .orElseThrow(() -> new NotFoundException(DEVICE_NOT_FOUND));
    }
}
