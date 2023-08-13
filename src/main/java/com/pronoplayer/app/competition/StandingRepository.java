package com.pronoplayer.app.competition;

import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pronoplayer.app.bo.lolesport.Standing;


public interface StandingRepository extends MongoRepository<Standing, String> {
    Optional<Standing> findByTournamentId(String tournamentId);
    Optional<Standing> findByCompetitionId(String competitionId);
}
