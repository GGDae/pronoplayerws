package com.pronoplayer.app.competition;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.PronoDay;
import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.lolesport.Event;
import com.pronoplayer.app.bo.lolesport.Match;
import com.pronoplayer.app.bo.lolesport.Response;
import com.pronoplayer.app.bo.lolesport.Schedule;
import com.pronoplayer.app.bo.lolesport.Standing;
import com.pronoplayer.app.bo.lolesport.Team;
import com.pronoplayer.app.prono.PronoWeekRepository;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
@EnableScheduling
public class CompetitionServiceImpl implements CompetitionService {
    private final CompetitionRepository competitionRepository;
    private final RestTemplate lolesportRestClient;
    private final PronoWeekRepository pronoWeekRepository;
    private final StandingRepository standingRepository;
    
    @Override
    public List<Competition> findAllCurrents() {
        return competitionRepository.findByCurrent(true).orElse(new ArrayList<>());
    }
    
    @Override
    public List<Competition> findAllByIds(List<String> ids) {
        return competitionRepository.findAllById(ids);
    }
    
    @Override
    public Competition findById(String id) {
        return competitionRepository.findById(id).orElse(null);
    }
    
    @Transactional
    @Scheduled(fixedRate = 600000)
    public void retrieveStandings() {
        List<Competition> currentLeagues = competitionRepository.findByCurrent(true).orElse(null);
        if (currentLeagues != null && currentLeagues.size() > 0) {
            for (Competition league : currentLeagues) {
                if (league != null && league.getTournamentId() != null) {
                    String url = "https://esports-api.lolesports.com/persisted/gw/getStandingsV3?hl=en-US&tournamentId=" + league.getTournamentId();
                    HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("X-Api-Key", "0TvQnueqKa5mxJntVWt0w4LpLfEkrV1Ta8rQBb9Z");
                    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<Response> response = lolesportRestClient.exchange(url, HttpMethod.GET, requestEntity, Response.class);
                    if (response.getStatusCode().is2xxSuccessful()) {
                        List<Standing> standings = response.getBody().getData().getStandings();
                        if (standings != null && standings.size() > 0) {
                            Standing standing = standings.get(0);
                            standing.setTournamentId(league.getTournamentId());
                            standing.setCompetitionId(league.getId());
                            Standing localStanding = standingRepository.findByTournamentId(league.getTournamentId()).orElse(standing);
                            localStanding.setStages(standing.getStages());
                            standingRepository.save(localStanding);
                        }
                    }
                }
            }
        }
    }
    
    @Override
    public Standing getStanding(String competitionId) {
        return standingRepository.findByCompetitionId(competitionId).orElse(null);
    }
    
    @Override
    @Transactional
    @Scheduled(fixedRate = 300000)
    @CacheEvict(value = "ranking", allEntries = true)
    public void loadFromLolesports() {
        List<Competition> currentLeagues = competitionRepository.findByCurrent(true).orElse(null);
        if (currentLeagues != null && currentLeagues.size() > 0) {
            for (Competition league : currentLeagues) {
                if (league != null && league.getLeagueId() != null) {
                    String url = "https://esports-api.lolesports.com/persisted/gw/getSchedule?hl=en-US&leagueId=" + league.getLeagueId();
                    HttpHeaders headers = new org.springframework.http.HttpHeaders();
                    headers.set("X-Api-Key", "0TvQnueqKa5mxJntVWt0w4LpLfEkrV1Ta8rQBb9Z");
                    HttpEntity<String> requestEntity = new HttpEntity<>(headers);
                    ResponseEntity<Response> response = lolesportRestClient.exchange(url, HttpMethod.GET, requestEntity, Response.class);
                    //Process the response
                    if (response.getStatusCode().is2xxSuccessful()) {
                        Schedule responseBody = response.getBody().getData().getSchedule();
                        for(Event event : responseBody.getEvents()) {
                            if ("match".equals(event.getType())) {
                                DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                                DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                                LocalDateTime ldt = event.getStartTime().toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                                if (league.getTournamentStartDate() != null) {
                                    LocalDateTime tournamentStartTime = LocalDateTime.parse(league.getTournamentStartDate(), formatter).atZone(ZoneId.systemDefault()).toLocalDateTime();
                                    if (ldt.isBefore(tournamentStartTime)) {
                                        continue;
                                    }
                                }
                                String period = getPeriod(ldt.format(formatter));
                                Optional<PronoWeek> pronoWeekOpt = pronoWeekRepository.findFirstByCompetitionIdAndBlockAndPeriod(league.getId(), event.getBlockName(), period);
                                PronoWeek pronoWeek;
                                if (pronoWeekOpt.isEmpty()) {
                                    pronoWeek = new PronoWeek();
                                    pronoWeek.setCompetitionId(league.getId());
                                    LocalDateTime ldtStart = ldt.minusDays(5);
                                    LocalDateTime ldtEnd = ldt.plusDays(2);
                                    pronoWeek.setStartDate(ldtStart.format(formatter));
                                    pronoWeek.setEndDate(ldtEnd.format(formatter));
                                    pronoWeek.setLockDate(ldt.format(formatter));
                                    pronoWeek.setBlock(event.getBlockName());
                                    pronoWeek.setPeriod(period);
                                    List<PronoDay> pronoDays = new ArrayList<>();
                                    pronoWeek.setPronoDays(pronoDays);
                                } else {
                                    pronoWeek = pronoWeekOpt.get();
                                }
                                Match m = event.getMatch();
                                m.setInProgress("inProgress".equals(event.getState()));
                                m.setState(event.getState());
                                m.setDateTime(event.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime().format(formatter));
                                List<String> scores = new ArrayList<>();
                                for(Team team : m.getTeams()) {
                                    if (team.getResult() != null) {
                                        scores.add("" + team.getResult().getGameWins());
                                        if ("win".equalsIgnoreCase(team.getResult().getOutcome())) {
                                            m.setResult(team.getCode());
                                        }
                                    }
                                }
                                Collections.sort(scores, Collections.reverseOrder());
                                m.setScore(String.join("-", scores));
                                
                                PronoDay pd = pronoWeek.getPronoDays().stream().filter(pronoDay -> pronoDay.getDate().equals(ldt.format(dayFormatter))).findFirst().orElse(null);
                                if (pd != null) {
                                    if (pd.getMatchs() == null) {
                                        pd.setMatchs(new ArrayList<>());
                                    }
                                    Match localMatch = pd.getMatchs().stream().filter(match -> match.getId().equals(event.getMatch().getId())).findFirst().orElse(null);
                                    int index = 0;
                                    for(int i = 0; i < pd.getMatchs().size(); i++) {
                                        if (pd.getMatchs().get(i).getId().equals(m.getId())) {
                                            index = i;
                                        }
                                    }
                                    if (localMatch == null) {
                                        pd.getMatchs().add(m);
                                    } else {
                                        if (!pd.getMatchs().get(index).getTeams().get(0).getCode().equals(m.getTeams().get(0).getCode())) {
                                            Collections.reverse(m.getTeams());
                                        }
                                        pd.getMatchs().set(index, m);
                                    }
                                } else {
                                    PronoDay pronoDay = new PronoDay();
                                    pronoDay.setDate(ldt.format(dayFormatter));
                                    pronoDay.setDay(pronoWeek.getPronoDays().size() + 1);
                                    List<Match> dayMatchs = new ArrayList<>();
                                    dayMatchs.add(m);
                                    pronoDay.setMatchs(dayMatchs);
                                    pronoWeek.getPronoDays().add(pronoDay);
                                }
                                pronoWeekRepository.save(pronoWeek);
                            }
                        };
                    } else {
                        // Handle errors or other status codes appropriately
                    }
                }
            }
        }
    }
    
    private String getPeriod(String date) {
        String[] s = date.split("-");
        String period = "";
        if (s.length > 1) {
            period = s[0] + s[1] + String.format("%2s", String.valueOf(Integer.parseInt(s[1]) + 1)).replace(' ', '0');
        }
        return period;
    }
}
