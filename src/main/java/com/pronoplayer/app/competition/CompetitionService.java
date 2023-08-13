package com.pronoplayer.app.competition;

import java.util.List;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.lolesport.Standing;

public interface CompetitionService {
    public List<Competition> findAllCurrents();
    public List<Competition> findAllByIds(List<String> ids);
    public Competition findById(String id);
    public Standing getStanding(String competitionId);
    public void loadFromLolesports();
}
