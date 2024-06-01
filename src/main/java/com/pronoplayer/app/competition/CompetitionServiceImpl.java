package com.pronoplayer.app.competition;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
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
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.lolesport.Cell;
import com.pronoplayer.app.bo.lolesport.Column;
import com.pronoplayer.app.bo.lolesport.Event;
import com.pronoplayer.app.bo.lolesport.Match;
import com.pronoplayer.app.bo.lolesport.Response;
import com.pronoplayer.app.bo.lolesport.Schedule;
import com.pronoplayer.app.bo.lolesport.Section;
import com.pronoplayer.app.bo.lolesport.Stage;
import com.pronoplayer.app.bo.lolesport.Standing;
import com.pronoplayer.app.bo.lolesport.Team;
import com.pronoplayer.app.discord.DiscordListener;
import com.pronoplayer.app.prono.PronoService;
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
    private final PronoService pronoService;
    
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
                            for (Stage stage : localStanding.getStages()) {
                                for (Section section : stage.getSections()) {
                                    if (section.getColumns() != null && !section.getColumns().isEmpty()) {
                                        for (Column column : section.getColumns()) {
                                            for (Cell cell : column.getCells()) {
                                                for(Match match : cell.getMatches()) {
                                                    PronoWeek week = pronoWeekRepository.findByPronoDaysMatchsIdAndPronoDaysMatchsDateTime(match.getId(), match.getDateTime()).orElse(null);
                                                    if (week != null) {
                                                        Match correspondingMatch = week.getPronoDays().stream().flatMap(day -> day.getMatchs().stream()).filter(m -> m.getId().equals(match.getId())).findFirst().orElse(null);
                                                        match.setDateTime(correspondingMatch.getDateTime());
                                                    }
                                                }
                                            }
                                        }
                                    }
                                }
                            }
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
    
    @Scheduled(initialDelay = 60000, fixedRate = 120000)
    public void remindUsers() {
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
        List<PronoWeek> pronoWeeks = pronoWeekRepository.findByPronoDaysFullDateGreaterThan(now.toString());
        if (pronoWeeks != null && !pronoWeeks.isEmpty()) {
            for(PronoWeek week : pronoWeeks) {
                for(int i = 0; i < week.getPronoDays().size(); i++) {
                    PronoDay day = week.getPronoDays().get(i);
                    if (day.getFullDate() != null) {
                        LocalDateTime matchDayDateTime = LocalDateTime.parse(day.getFullDate(), formatter);
                        // LocalDateTime fakeNow = LocalDateTime.parse("2024-01-13T19:00:00Z", formatter);
                        long minutesDifference = java.time.Duration.between(now, matchDayDateTime).toMinutes();
                        if (minutesDifference <= (300 + 60*matchDayDateTime.getHour()) && minutesDifference > (298 + 60*matchDayDateTime.getHour())) {
                            Competition comp = competitionRepository.findById(week.getCompetitionId()).get();
                            if (comp != null) {
                                String block;
                                if (week.getBlock() != null && week.getBlock().length() > 1) {
                                    block = week.getBlock().substring(0, 1).toUpperCase() + week.getBlock().substring(1);
                                } else {
                                    block = week.getBlock();
                                }
                                if (week.getPronoDays().size() > 1) {
                                    block = block + " - " + "Day " + (i + 1);
                                }
                                this.sendPMToUsers(week, day, comp, block, matchDayDateTime);
                                DiscordListener.sendMessage("**" + comp.getName() + " - " + block + "** à <t:" + matchDayDateTime.toEpochSecond(ZoneOffset.UTC) + ":t>. N'oubliez pas vos pronos !", comp.getId());
                            }
                        } else if (minutesDifference <= 60 && minutesDifference > 58) {
                            Competition comp = competitionRepository.findById(week.getCompetitionId()).get();
                            if (comp != null) {
                                String block;
                                if (week.getBlock() != null && week.getBlock().length() > 1) {
                                    block = week.getBlock().substring(0, 1).toUpperCase() + week.getBlock().substring(1);
                                } else {
                                    block = week.getBlock();
                                }
                                if (week.getPronoDays().size() > 1) {
                                    block = block + " - " + "Day " + (i + 1);
                                }
                                this.sendPMToUsers(week, day, comp, block, matchDayDateTime);
                            }
                        }
                    }
                }
            }
        }
    }
    
    private void sendPMToUsers(PronoWeek week, PronoDay day, Competition comp, String block, LocalDateTime matchDayDateTime) {
        List<User> usersToRemind = this.pronoService.getUsersThatForgotToPronoTheDay(week, day);
        for(User user : usersToRemind) {
            if (user.getDiscord() != null && user.getDiscord().getReminder() && user.getDiscord().getId() != null && user.getDiscord().getCompetitions() != null && (user.getDiscord().getCompetitions().contains(comp.getId()) || user.getDiscord().getCompetitions().contains(comp.getName()))) {
                DiscordListener.sendPM(user, "Tu n'as pas validé tous tes pronos pour **" + comp.getName() + " - " + block + "** à <t:" + matchDayDateTime.toEpochSecond(ZoneOffset.UTC) + ":t> ! ");
            }
        }
    }
    
    @Override
    @Transactional
    // @Scheduled(fixedRate = 150000)
    @Scheduled(initialDelay = 4000, fixedRate = 150000)
    @CacheEvict(value = {"ranking", "publicGroups"}, allEntries = true)
    public void loadFromLolesports() {
        // TESTING
        // DiscordListener.sendPM();
        // pronoService.computeStats(new Competition(), new Match(), false);
        // TESTING
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
                        DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd");
                        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss'Z'");
                        Schedule responseBody = response.getBody().getData().getSchedule();
                        LocalDateTime maxFutureDate = LocalDateTime.now().plusDays(8);
                        for(Event event : responseBody.getEvents()) {
                            if ("match".equals(event.getType())) {
                                LocalDateTime ldt = event.getStartTime().toInstant().atOffset(ZoneOffset.UTC).toLocalDateTime();
                                if (league.getTournamentStartDate() != null) {
                                    LocalDateTime tournamentStartTime = LocalDateTime.parse(league.getTournamentStartDate(), formatter).atOffset(ZoneOffset.UTC).toLocalDateTime();
                                    if (ldt.isBefore(tournamentStartTime) || ldt.isAfter(maxFutureDate)) {
                                        continue;
                                    }
                                }
                                String period = getPeriod(ldt.format(formatter));
                                Optional<PronoWeek> pronoWeekOpt = pronoWeekRepository.findFirstByCompetitionIdAndBlockAndPeriod(league.getId(), event.getBlockName(), period);
                                PronoWeek pronoWeek;
                                if (pronoWeekOpt.isEmpty()) {
                                    pronoWeek = new PronoWeek();
                                    pronoWeek.setCompetitionId(league.getId());
                                    // LocalDateTime ldtStart = ldt.minusDays(5);
                                    // LocalDateTime ldtEnd = ldt.plusDays(2);
                                    // pronoWeek.setStartDate(ldtStart.format(formatter));
                                    // pronoWeek.setEndDate(ldtEnd.format(formatter));
                                    pronoWeek.setLockDate(ldt.format(formatter));
                                    pronoWeek.setBlock(event.getBlockName());
                                    pronoWeek.setPeriod(period);
                                    List<PronoDay> pronoDays = new ArrayList<>();
                                    pronoWeek.setPronoDays(pronoDays);
                                } else {
                                    pronoWeek = pronoWeekOpt.get();
                                }
                                Match m = event.getMatch();
                                if ("inProgress".equals(event.getState())) {
                                    m.setLocked(true);
                                }
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
                                    if (pd.getFullDate() == null) {
                                        pd.setFullDate(ldt.format(formatter));
                                    }
                                    if (pd.getMatchs() == null) {
                                        pd.setMatchs(new ArrayList<>());
                                    }
                                    Match localMatch = pd.getMatchs().stream().filter(match -> match.getId().equals(event.getMatch().getId())).findFirst().orElse(null);
                                    if (localMatch == null) {
                                        pd.getMatchs().add(m);
                                        LocalDateTime now = LocalDateTime.now(ZoneOffset.UTC);
                                        LocalDateTime matchDayDateTime = LocalDateTime.parse(m.getDateTime(), formatter);
                                        long minutesDifference = java.time.Duration.between(now, matchDayDateTime).toMinutes();
                                        if (minutesDifference >= 0 && minutesDifference <= 90) {
                                            Competition comp = competitionRepository.findById(pronoWeek.getCompetitionId()).get();
                                            if (comp != null && m.getTeams().size() > 1) {
                                                DiscordListener.sendMessage("Un match de **" + comp.getName() + "** vient d'être ajouté à la dernière minute !" + m.getTeams().get(0).getCode() + " - " + m.getTeams().get(1).getCode() + " à <t:" + matchDayDateTime.toEpochSecond(ZoneOffset.UTC) + ":t>.", comp.getId());
                                            }
                                        }
                                    } else {
                                        int index = 0;
                                        int nearestMatchIndex = 0;
                                        if (!localMatch.isLocked() && m.isLocked()) {
                                            pronoService.computeStats(league, m, false);
                                        }
                                        if (localMatch.isLocked()) {
                                            m.setLocked(true);
                                        }
                                        if (localMatch.getResult() == null && m.getResult() != null) {
                                            //on vient update avec un résultat depuis lolesports. On va chercher le prochain match de la journée et le considérer "inProgress" pour le lock
                                            // Find the nearest next match
                                            Match nearestMatch = null;
                                            long minTimeDifference = Long.MAX_VALUE;
                                            
                                            for (int i = 0; i < pd.getMatchs().size(); i++) {
                                                long timeDifference = ChronoUnit.SECONDS.between(event.getStartTime().toInstant().atZone(ZoneId.of("UTC")).toLocalDateTime(), LocalDateTime.parse(pd.getMatchs().get(i).getDateTime(), formatter).atOffset(ZoneOffset.UTC).toLocalDateTime());
                                                if (timeDifference > 0 && timeDifference < minTimeDifference) {
                                                    nearestMatch = pd.getMatchs().get(i);
                                                    nearestMatchIndex = i;
                                                    minTimeDifference = timeDifference;
                                                }
                                            }
                                            if (nearestMatch != null) {
                                                if (!nearestMatch.isLocked()) {
                                                    // si le prochain match n'est pas encore commencé, on le lock et on envoie les stats. s'il a déjà été update et lock avant que le résultat du match précédent soit mis, on ne fait rien
                                                    nearestMatch.setLocked(true);
                                                    pronoService.computeStats(league, nearestMatch, true);
                                                    pd.getMatchs().set(nearestMatchIndex, nearestMatch);
                                                }
                                            }
                                        }
                                        for(int i = 0; i < pd.getMatchs().size(); i++) {
                                            if (pd.getMatchs().get(i).getId().equals(m.getId())) {
                                                index = i;
                                            }
                                        }
                                        if (!pd.getMatchs().get(index).getTeams().get(0).getCode().equals(m.getTeams().get(0).getCode())) {
                                            Collections.reverse(m.getTeams());
                                        }
                                        pd.getMatchs().set(index, m);
                                        
                                    }
                                } else {
                                    PronoDay pronoDay = new PronoDay();
                                    pronoDay.setDate(ldt.format(dayFormatter));
                                    pronoDay.setFullDate(ldt.format(formatter));
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
        if (s.length > 2) {
            int year = 0;
            int firstMonth = 0;
            int secondMonth = 0;
            int day = Integer.parseInt(s[2].split("T")[0]);
            if (day < 7) {
                year = Integer.parseInt(s[0]) - 1;
                if (Integer.parseInt(s[1]) == 1) {
                    firstMonth = 12;
                    secondMonth = Integer.parseInt(s[1]) + 1;
                } else {
                    firstMonth = Integer.parseInt(s[1]) - 1;
                    secondMonth = Integer.parseInt(s[1]);
                }
            } else {
                year = Integer.parseInt(s[0]);
                firstMonth = Integer.parseInt(s[1]);
                secondMonth = Integer.parseInt(s[1]) + 1;
            }
            // period = s[0] + s[1] + String.format("%2s", String.valueOf(Integer.parseInt(s[1]) + 1)).replace(' ', '0');
            period = String.valueOf(year) + String.format("%2s", String.valueOf(firstMonth)).replace(' ', '0') + String.format("%2s", String.valueOf(secondMonth)).replace(' ', '0');
        }
        return period;
    }
}
