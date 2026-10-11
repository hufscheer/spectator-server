package com.sports.server.command.push.application;

import static com.sports.server.command.push.exception.PushErrorMessages.DEVICE_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.FOLLOW_GAME_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.FOLLOW_TEAM_NOT_FOUND;
import static com.sports.server.command.push.exception.PushErrorMessages.GAME_FOLLOW_LIMIT_EXCEEDED;
import static com.sports.server.command.push.exception.PushErrorMessages.TEAM_FOLLOW_LIMIT_EXCEEDED;

import com.sports.server.command.game.domain.GameRepository;
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
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
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

    private final PushDeviceRepository pushDeviceRepository;
    private final PushFollowRepository pushFollowRepository;
    private final TeamRepository teamRepository;
    private final GameRepository gameRepository;
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
        return new PushFollowsResponse(
                targetIdsOf(follows, FollowTargetType.TEAM),
                targetIdsOf(follows, FollowTargetType.GAME)
        );
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
        validateFollows(teamIds, gameIds);

        pushFollowRepository.deleteAllByDeviceId(device.getId());
        pushFollowRepository.saveAll(followsOf(device, teamIds, gameIds));
    }

    private void validateFollows(Set<Long> teamIds, Set<Long> gameIds) {
        if (teamIds.size() > MAX_FOLLOWS_PER_TYPE) {
            throw new BadRequestException(TEAM_FOLLOW_LIMIT_EXCEEDED);
        }
        if (gameIds.size() > MAX_FOLLOWS_PER_TYPE) {
            throw new BadRequestException(GAME_FOLLOW_LIMIT_EXCEEDED);
        }
        if (teamRepository.countByIdIn(teamIds) != teamIds.size()) {
            throw new BadRequestException(FOLLOW_TEAM_NOT_FOUND);
        }
        if (gameRepository.countByIdIn(gameIds) != gameIds.size()) {
            throw new BadRequestException(FOLLOW_GAME_NOT_FOUND);
        }
    }

    private List<PushFollow> followsOf(PushDevice device, Collection<Long> teamIds, Collection<Long> gameIds) {
        Stream<PushFollow> teams = teamIds.stream()
                .map(id -> new PushFollow(device.getId(), FollowTargetType.TEAM, id));
        Stream<PushFollow> games = gameIds.stream()
                .map(id -> new PushFollow(device.getId(), FollowTargetType.GAME, id));
        return Stream.concat(teams, games).toList();
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
