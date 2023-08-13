package com.pronoplayer.app.competition;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;

import com.pronoplayer.app.bo.Competition;

public interface CompetitionRepository extends MongoRepository<Competition, String> {
    public Optional<Competition> findByNameAndCurrent(String name, boolean current);
    
    public Optional<List<Competition>> findByCurrent(boolean current);
}
