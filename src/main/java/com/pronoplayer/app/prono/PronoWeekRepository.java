package com.pronoplayer.app.prono;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import com.pronoplayer.app.bo.PronoWeek;


public interface PronoWeekRepository extends MongoRepository<PronoWeek, String> {
    
    public Optional<PronoWeek> findFirstByCompetitionId(String competitionId);
    
    public Optional<PronoWeek> findFirstByCompetitionIdAndBlockAndPeriod(String competitionId, String block, String period);
    
    @Query("{ 'startDate' : { $lt: ?0 }, 'endDate' : { $gt: ?0 }, 'competitionId' : ?1 }")
    public Optional<PronoWeek> findFirstByStartDateBeforeAndEndDateAfterAndCompetitionId(String date, String competitionId);
    
    public Optional<List<PronoWeek>> findByCompetitionId(String competitionId);

    public Optional<PronoWeek> findByPronoDaysMatchsId(String matchId);
}