package com.sports.server.command.league.domain;

import org.springframework.data.repository.Repository;

import java.util.List;

public interface LeagueTopScorerRepository extends Repository<LeagueTopScorer, Long> {
    LeagueTopScorer save(LeagueTopScorer leagueTopScorer);
    
    void delete(LeagueTopScorer leagueTopScorer);
    
    List<LeagueTopScorer> findByLeagueId(Long leagueId);
    
    void deleteByLeagueId(Long leagueId);
}