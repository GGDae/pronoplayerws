package com.pronoplayer.app.prono;



import java.util.List;
import java.util.Map;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.PronoDay;
import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;
import com.pronoplayer.app.bo.Traitor;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.lolesport.Match;

public interface PronoService {
    public PronoWeek getPronoByCompetitionIdAndWeek(String competitionId, String week, String period);
    
    public PronoWeek getPronoByCompetitionIdAndDate(String competitionId, String date);
    
    public Pronostic getPronoForUser(String competitionId, String groupId, String weekId, String userId);

    public List<PronoWeek> getPronoWeeksByCompetitionId(String competitionId);
    
    public Pronostic updateScores(String userId, Pronostic pronostic);

    public void generateRankingImage(String groupId, String competitionId);
    
    public Map<String, Integer[]> getRanking(String groupId, String competitionId);

    public void computeStats(Competition league, Match match, boolean isFuture);

    public List<User> getUsersThatForgotToPronoTheDay(PronoWeek week, PronoDay day);

    public List<Traitor> findTheTraitors(String group, String belovedTeam, String name, String year, String split);
}
