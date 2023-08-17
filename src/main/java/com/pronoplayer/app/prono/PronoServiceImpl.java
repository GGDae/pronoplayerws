package com.pronoplayer.app.prono;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.MatchScore;
import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;
import com.pronoplayer.app.bo.lolesport.Match;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PronoServiceImpl implements PronoService {
    private final PronoRepository pronoRepository;
    private final PronoWeekRepository pronoWeekRepository;
    
    @Override
    public PronoWeek getPronoByCompetitionIdAndWeek(String competitionId, String block, String period) {
        return pronoWeekRepository.findFirstByCompetitionIdAndBlockAndPeriod(competitionId, block, period).orElse(null);
    }
    
    @Override
    public PronoWeek getPronoByCompetitionIdAndDate(String competitionId, String date) {
        return pronoWeekRepository.findFirstByStartDateBeforeAndEndDateAfterAndCompetitionId(date, competitionId).orElse(null);
    }
    
    @Override
    public List<PronoWeek> getPronoWeeksByCompetitionId(String competitionId) {
        return pronoWeekRepository.findByCompetitionId(competitionId).orElse(null);
    }
    
    @Override
    public Pronostic getPronoForUser(String competitionId, String groupId, String weekId, String userId) {
        return pronoRepository.findByCompetitionIdAndGroupIdAndWeekIdAndUserId(competitionId, groupId, weekId, userId).orElse(null);
    }
    
    private List<Pronostic> getPronoForCompetitionAndGroup(String competitionId, String groupId) {
        return pronoRepository.findByCompetitionIdAndGroupId(competitionId, groupId).orElse(null);
    }
    
    @Override
    public Pronostic updateScores(String userId, Pronostic pronostic) {
        Pronostic prono = pronoRepository.findByCompetitionIdAndGroupIdAndWeekIdAndUserId(pronostic.getCompetitionId(), pronostic.getGroupId(), pronostic.getWeekId(), userId).orElse(null);
        if (pronostic.getScores() != null && pronostic.getScores().size() > 0) {
            PronoWeek week = pronoWeekRepository.findByPronoDaysMatchsId(pronostic.getScores().get(0).getMatchId()).orElse(null);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
            List<MatchScore> scoresToSave = new ArrayList<>();
            if (week != null) {
                for(MatchScore ms : pronostic.getScores()) {
                    String msId = ms.getMatchId();
                    Match correspondingMatch = week.getPronoDays().stream().flatMap(day -> day.getMatchs().stream()).filter(match -> match.getId().equals(msId)).findFirst().orElse(null);
                    if (correspondingMatch != null) {
                        LocalDateTime matchTime = LocalDateTime.parse(correspondingMatch.getDateTime(), formatter).atZone(ZoneId.systemDefault()).toLocalDateTime();
                        LocalDateTime now = LocalDateTime.now();
                        if (matchTime.isBefore(now)) {
                            if (prono != null) {
                                MatchScore savedMs = prono.getScores().stream().filter(s -> s.getMatchId().equals(msId)).findFirst().orElse(null);
                                if (savedMs != null) {
                                    scoresToSave.add(savedMs);
                                }
                            }
                        } else {
                            scoresToSave.add(ms);
                        }
                    }
                }
                prono.setScores(scoresToSave);
                return pronoRepository.save(prono);
            }
        }
        if (prono == null) {
            return pronoRepository.save(pronostic);
        }
        return null;
    }
    
    @Override
    @Cacheable(cacheNames = "ranking", key = "#groupId.concat('-').concat(#competitionId)")
    public Map<String, Integer> getRanking(String groupId, String competitionId) {
        Map<String, Integer> ranking = new HashMap<>();
        List<Pronostic> pronos = this.getPronoForCompetitionAndGroup(competitionId, groupId);
        List<PronoWeek> weeks = pronoWeekRepository.findByCompetitionId(competitionId).orElse(null);
        Map<String, List<MatchScore>> results = new HashMap<>();
        if (pronos != null && weeks != null) {
            Collections.sort(weeks, new Comparator<PronoWeek>() {
                @Override
                public int compare(PronoWeek o1, PronoWeek o2) {
                    return o1.getStartDate().compareTo(o2.getStartDate());
                }
            });
            weeks.forEach(week -> {
                List<MatchScore> matchScores = new ArrayList<>();
                week.getPronoDays().forEach(pronoDay -> {
                    for(Match match : pronoDay.getMatchs()) {
                        if (match.getResult() != null) {
                            MatchScore ms = new MatchScore();
                            ms.setMatchId(match.getId());
                            ms.setWinner(match.getResult());
                            ms.setBo(match.getStrategy().getCount() > 1);
                            ms.setScore(match.getScore());
                            matchScores.add(ms);
                        }
                    }
                });
                results.put(week.getId(), matchScores);
            });
            for(Pronostic prono : pronos) {
                if (ranking.get(prono.getUserId()) == null) {
                    ranking.put(prono.getUserId(), 0);
                }
                if (results.get(prono.getWeekId()) != null) {
                    List<MatchScore> resultsDay = results.get(prono.getWeekId());
                    for(MatchScore ms : prono.getScores()) {
                        MatchScore result = resultsDay.stream().filter(rd -> rd.getMatchId().equals(ms.getMatchId())).findFirst().orElse(null);
                        if (result != null && result.getWinner() != null && result.getWinner().equals(ms.getWinner())) {
                            Integer points = 1;
                            String score = ms.getScore();
                            String reversed = new StringBuilder(score).reverse().toString();
                            if (result.isBo() && (result.getScore().equals(score) || result.getScore().equals(reversed))) {
                                points = points + 1;
                            }
                            ranking.put(prono.getUserId(), ranking.get(prono.getUserId()) + points);
                        }
                    }
                    
                }
            }
        }
        return ranking;
    }
}
