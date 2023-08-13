package com.pronoplayer.app.competition;

import java.util.List;

import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.lolesport.Standing;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class CompetitionControllerImpl implements CompetitionController {
    
    private final CompetitionService competitionService;
    
    @Override
    public List<Competition> findAllCurrents() {
        return competitionService.findAllCurrents();
    }
    
    @Override
    public List<Competition> findAllByIds(List<String> ids) {
        return competitionService.findAllByIds(ids);
    }
    
    @Override
    public Competition findById(String id) {
        return competitionService.findById(id);
    }
    
    @Override
    public void loadFromLolesports() {
        competitionService.loadFromLolesports();
    }

    @Override
    public Standing getStanding(String competitionId) {
        return competitionService.getStanding(competitionId);
    }
    
}
