package com.pronoplayer.app.prono;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pronoplayer.app.bo.Pronostic;



public interface PronoRepository extends MongoRepository<Pronostic, String> {
    public Optional<List<Pronostic>> findByCompetitionIdAndGroupId(String competitionId, String groupId);
    public Optional<Pronostic> findByCompetitionIdAndGroupIdAndWeekIdAndUserId(String competitionId, String groupId, String weekId, String userId);
    public Optional<List<Pronostic>> findByCompetitionIdAndGroupIdAndScoresMatchId(String competitionId, String groupId, String matchId);
    public Optional<List<Pronostic>> findByWeekId(String weekId);
    public Optional<List<Pronostic>> findByGroupIdAndScoresMatchIdIn(String groupId, List<String> matchIds);
    // public Optional<Pronostic> findByCompetitionIdAndGroupIdAndWeekIdAndDayAndUserId(String competitionId, String groupId, String weekId, int day, String userId);
}
