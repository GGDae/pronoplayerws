package com.pronoplayer.app.prono;



import java.util.List;
import java.util.Map;

import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;

public interface PronoService {
    public PronoWeek getPronoByCompetitionIdAndWeek(String competitionId, String week, String period);
    
    public PronoWeek getPronoByCompetitionIdAndDate(String competitionId, String date);
    
    public Pronostic getPronoForUser(String competitionId, String groupId, String weekId, String userId);

    public List<PronoWeek> getPronoWeeksByCompetitionId(String competitionId);
    
    public Pronostic updateScores(String userId, Pronostic pronostic);
    
    public Map<String, Integer> getRanking(String groupId, String competitionId);
}
