package com.pronoplayer.app.prono;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.Map.Entry;

import javax.imageio.ImageIO;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.Group;
import com.pronoplayer.app.bo.MatchScore;
import com.pronoplayer.app.bo.MatchStat;
import com.pronoplayer.app.bo.PronoDay;
import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;
import com.pronoplayer.app.bo.Traitor;
import com.pronoplayer.app.bo.User;
import com.pronoplayer.app.bo.lolesport.Match;
import com.pronoplayer.app.competition.CompetitionRepository;
import com.pronoplayer.app.discord.DiscordListener;
import com.pronoplayer.app.group.GroupService;
import com.pronoplayer.app.user.UserService;

import java.awt.Color;
import java.awt.Font;
import java.awt.FontFormatException;
import java.awt.Graphics2D;
import java.awt.RenderingHints;
import java.awt.image.BufferedImage;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Service
public class PronoServiceImpl implements PronoService {
    private final PronoRepository pronoRepository;
    private final PronoWeekRepository pronoWeekRepository;
    private final UserService userService;
    private final GroupService groupService;
    private final CompetitionRepository competitionRepository;
    
    @Value("classpath:/static/test_image_classement.png")
    Resource rankingImageResource;
    
    @Value("classpath:/static/game_stats.png")
    Resource statsImageResource;
    
    @Value("classpath:/font/EASPORTS15.ttf")
    Resource eaFont;
    
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
            // PronoWeek week = pronoWeekRepository.findByPronoDaysMatchsId(pronostic.getScores().get(0).getMatchId()).orElse(null);
            PronoWeek week = pronoWeekRepository.findById(pronostic.getWeekId()).orElse(null);
            DateTimeFormatter formatter = DateTimeFormatter.ISO_DATE_TIME;
            List<MatchScore> scoresToSave = new ArrayList<>();
            if (week != null) {
                for(MatchScore ms : pronostic.getScores()) {
                    String msId = ms.getMatchId();
                    Match correspondingMatch = week.getPronoDays().stream().flatMap(day -> day.getMatchs().stream()).filter(match -> match.getId().equals(msId)).findFirst().orElse(null);
                    if (correspondingMatch != null) {
                        // LocalDateTime matchTimeLocalDate = LocalDateTime.parse(correspondingMatch.getDateTime(), formatter);
                        ZonedDateTime matchTimeUTC = LocalDateTime.parse(correspondingMatch.getDateTime(), formatter).atZone(ZoneOffset.UTC);
                        ZonedDateTime matchTime = matchTimeUTC.withZoneSameInstant(ZoneId.systemDefault());
                        ZonedDateTime now = ZonedDateTime.now(ZoneId.systemDefault());
                        if (correspondingMatch.isLocked() || matchTime.isBefore(now)) {
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
    public void generateRankingImage(String groupId, String competitionId) {
        Map<String, Integer[]> ranking = getRanking(groupId, competitionId);
        Competition competition = competitionRepository.findById(competitionId).get();
        List<Entry<String, Integer[]>> entryList = new ArrayList<>(ranking.entrySet());
        Collections.sort(entryList, new Comparator<Entry<String, Integer[]>>() {
            @Override
            public int compare(Entry<String, Integer[]> entry1, Entry<String, Integer[]> entry2) {
                int scoreCompare = entry2.getValue()[0].compareTo(entry1.getValue()[0]);
                if (scoreCompare == 0) {
                    scoreCompare = Float.valueOf((float) entry2.getValue()[1] / entry2.getValue()[2]).compareTo((float) entry1.getValue()[1] / entry1.getValue()[2]);
                }
                return scoreCompare;
            }
        });
        Map<String, Integer[]> sortedMap = new LinkedHashMap<>();
        for (Entry<String, Integer[]> entry : entryList) {
            sortedMap.put(entry.getKey(), entry.getValue());
        }
        
        try {
            BufferedImage templateImage = ImageIO.read(rankingImageResource.getInputStream());
            Graphics2D graphics = templateImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, eaFont.getInputStream());
            customFont = customFont.deriveFont(Font.PLAIN, 44);
            graphics.setFont(customFont);
            
            // contour
            graphics.setColor(Color.BLACK);
            graphics.drawString(competition.getName() + " " + competition.getSplit() + " " + competition.getYear(), 220 + 3, 180 + 3);
            graphics.drawString(competition.getName() + " " + competition.getSplit() + " " + competition.getYear(), 220 + 3, 180 - 3);
            graphics.drawString(competition.getName() + " " + competition.getSplit() + " " + competition.getYear(), 220 - 3, 180 + 3);
            graphics.drawString(competition.getName() + " " + competition.getSplit() + " " + competition.getYear(), 220 - 3, 180 - 3);
            
            graphics.setColor(new Color(217, 204, 240));
            graphics.drawString(competition.getName() + " " + competition.getSplit() + " " + competition.getYear(), 220, 180);
            
            Font rankFont = new Font("Helvetica", Font.BOLD, 30);
            graphics.setFont(rankFont);
            graphics.setColor(Color.BLACK);
            
            int cpt = 0;
            int rank = 0;
            int lastScore = 0;
            Float lastScore2 = 0F;
            for(Entry<String, Integer[]> entry : sortedMap.entrySet()) {
                if (cpt < 10) {
                    if (lastScore == 0 || lastScore != entry.getValue()[0] || (lastScore == entry.getValue()[0] && !Float.valueOf((float) entry.getValue()[1] / entry.getValue()[2]).equals(lastScore2))) {
                        rank = cpt + 1;
                    }
                    User user = this.userService.getUserByUserId(entry.getKey());
                    BufferedImage userLogo = ImageIO.read(new URI(user.getProfileImageUrl()).toURL());
                    if (rank == (cpt + 1)) {
                        graphics.drawString("" + rank, 110, 280 + (65 * cpt));
                    }
                    graphics.drawImage(userLogo, 160, 245 + (65 * cpt), 50, 50, null);
                    
                    graphics.setColor(Color.BLACK);
                    graphics.drawString(user.getDisplayName(), 240, 280 + (65 * cpt));
                    graphics.drawString(entry.getValue()[0] + " pts", 590, 280 + (65 * cpt));
                    lastScore = entry.getValue()[0];
                    lastScore2 = (float) entry.getValue()[1] / entry.getValue()[2];
                    cpt++;
                } else {
                    break;
                }
            }
            graphics.dispose();
            // File outputFile = new File(this.getClass().getResource("/").getPath() + "/static/snapshot_" + groupId + "_" + competitionId + ".png");
            InputStream inputStream = writeToInputStream(templateImage, "png");
            DiscordListener.sendSnapshot(groupId, competitionId, inputStream);
        } catch (FontFormatException | IOException |URISyntaxException e) {
            e.printStackTrace();
        }
        
    }
    
    @Override
    @Cacheable(cacheNames = "ranking", key = "#groupId.concat('-').concat(#competitionId)")
    public Map<String, Integer[]> getRanking(String groupId, String competitionId) {
        Map<String, Integer[]> ranking = new HashMap<>();
        List<Pronostic> pronos = this.getPronoForCompetitionAndGroup(competitionId, groupId);
        List<PronoWeek> weeks = pronoWeekRepository.findByCompetitionId(competitionId).orElse(null);
        Map<String, List<MatchScore>> results = new HashMap<>();
        final AtomicInteger total = new AtomicInteger(0);
        if (pronos != null && weeks != null) {
            weeks.forEach(week -> {
                List<MatchScore> matchScores = new ArrayList<>();
                week.getPronoDays().forEach(pronoDay -> {
                    for(Match match : pronoDay.getMatchs()) {
                        if (match.getResult() != null) {
                            MatchScore ms = new MatchScore();
                            ms.setMatchId(match.getId());
                            ms.setDateTime(match.getDateTime());
                            ms.setWinner(match.getResult());
                            ms.setBo(match.getStrategy().getCount() > 1);
                            ms.setScore(match.getScore());
                            matchScores.add(ms);
                            total.incrementAndGet();
                        }
                    }
                });
                results.put(week.getId(), matchScores);
            });
            for(Pronostic prono : pronos) {
                if (ranking.get(prono.getUserId()) == null) {
                    Integer[] array = {0, 0, 0};
                    ranking.put(prono.getUserId(), array);
                }
                if (results.get(prono.getWeekId()) != null) {
                    List<MatchScore> resultsDay = results.get(prono.getWeekId());
                    for(MatchScore ms : prono.getScores()) {
                        MatchScore result = resultsDay.stream().filter(rd -> rd.getMatchId().equals(ms.getMatchId())).findFirst().orElse(null);
                        ranking.get(prono.getUserId())[2] += 1;
                        if (result != null) {
                            if (result.getWinner() != null && result.getWinner().equals(ms.getWinner())) {
                                Integer points = 1;
                                String score = ms.getScore();
                                String reversed = new StringBuilder(score).reverse().toString();
                                if (result.isBo() && (result.getScore().equals(score) || result.getScore().equals(reversed))) {
                                    points = points + 1;
                                }
                                ranking.get(prono.getUserId())[1] += 1;
                                ranking.get(prono.getUserId())[0] += points;
                            }
                        }
                    }
                    
                }
            }
        }
        for (Map.Entry<String, Integer[]> entry : ranking.entrySet()) {
            String key = entry.getKey();
            Integer[] array = entry.getValue();
            array[2] = total.get();
            ranking.put(key, array);
        }
        return ranking;
    }
    
    public void computeStats(Competition league, Match match, boolean isFuture) {
        List<Group> groups = groupService.findAll();
        // TESTING
        // league.setId("651c68d104cd6c72640433fa");
        // match.setId("110852960170020343");
        // Team t1 = new Team();
        // t1.setCode("R7");
        // t1.setImage("http://static.lolesports.com/teams/1673451007201_IMG_3488.png");
        // Team t2 = new Team();
        // t2.setCode("BDS");
        // t2.setImage("http://static.lolesports.com/teams/1641944663689_bdslogo.png");
        
        // ArrayList<Team> aaaa = new ArrayList<>();
        // aaaa.add(t1);
        // aaaa.add(t2);
        // match.setTeams(aaaa);
        // TESTING
        MatchStat team1 = new MatchStat();
        team1.setTeam(match.getTeams().get(0));
        team1.setNbVotes(0);
        MatchStat team2 = new MatchStat();
        team2.setTeam(match.getTeams().get(1));
        team2.setNbVotes(0);
        Integer total = 0;
        groups = groups.stream().filter(g -> g.getCompetitions().contains(league.getId())).toList();
        for(Group group : groups) {
            Optional<List<Pronostic>> pronosForMatch = pronoRepository.findByCompetitionIdAndGroupIdAndScoresMatchId(league.getId(), group.getId(), match.getId());
            if (pronosForMatch.isPresent()) {
                for(Pronostic pronostic : pronosForMatch.get()) {
                    MatchScore userProno = pronostic.getScores().stream().filter(matchScore -> matchScore.getMatchId().equals(match.getId())).toList().get(0);
                    if (userProno.getWinner() != null) {
                        if (userProno.getWinner().equals(team1.getTeam().getCode())) {
                            team1.setNbVotes(team1.getNbVotes() + 1);
                        } else {
                            team2.setNbVotes(team2.getNbVotes() + 1);
                        }
                        total += 1;
                    }
                }
            }
            team1.setVotePercent(Double.valueOf(team1.getNbVotes()) / Double.valueOf(total));
            team2.setVotePercent(Double.valueOf(team2.getNbVotes()) / Double.valueOf(total));
            generateAndSendStatsImage(group, team1, team2, match.getId(), league.getId(), isFuture);
        }
    }
    
    private void generateAndSendStatsImage(Group group, MatchStat team1, MatchStat team2, String matchId, String competitionId, boolean isFuture) {
        try {
            BufferedImage templateImage = ImageIO.read(statsImageResource.getInputStream());
            NumberFormat formatter = new DecimalFormat("#0.0");
            Graphics2D graphics = templateImage.createGraphics();
            graphics.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
            graphics.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, RenderingHints.VALUE_TEXT_ANTIALIAS_ON);
            Font customFont = Font.createFont(Font.TRUETYPE_FONT, eaFont.getInputStream());
            
            Font percentFont = new Font("Arial", Font.BOLD, 28);
            
            customFont = customFont.deriveFont(Font.PLAIN, 44);
            graphics.setFont(customFont);
            graphics.setColor(Color.BLACK);
            // contours des textes
            graphics.drawString(team1.getTeam().getCode(), 230 + 3, 220 + 3);
            graphics.drawString(team1.getTeam().getCode(), 230 + 3, 220 - 3);
            graphics.drawString(team1.getTeam().getCode(), 230 - 3, 220 + 3);
            graphics.drawString(team1.getTeam().getCode(), 230 - 3, 220 - 3);
            graphics.drawString(team2.getTeam().getCode(), 485 + 3, 220 + 3);
            graphics.drawString(team2.getTeam().getCode(), 485 + 3, 220 - 3);
            graphics.drawString(team2.getTeam().getCode(), 485 - 3, 220 + 3);
            graphics.drawString(team2.getTeam().getCode(), 485 - 3, 220 - 3);
            
            
            
            String image1 = team1.getTeam().getImage().replace("http://", "https://");
            String image2 = team2.getTeam().getImage().replace("http://", "https://");
            BufferedImage team1Logo = ImageIO.read(new URI(image1).toURL());
            BufferedImage team2Logo = ImageIO.read(new URI(image2).toURL());
            
            graphics.setColor(new Color(103, 58, 183));
            graphics.drawString(team1.getTeam().getCode(), 230, 220);
            graphics.drawString(team2.getTeam().getCode(), 485, 220);
            
            graphics.drawImage(team1Logo, 100, 140, 100, 100, null);
            graphics.drawImage(team2Logo, 580, 140, 100, 100, null);
            
            double team1Size = (582 * team1.getVotePercent());
            graphics.fillRect(100, 249, (int)Math.round(team1Size), 80);
            
            if (team1.getVotePercent() != null || team2.getVotePercent() != null) {
                graphics.setFont(percentFont);
                
                graphics.setColor(Color.BLACK);
                if (team1.getVotePercent() != null) {
                    graphics.drawString(formatter.format(team1.getVotePercent() * 100) + "%", 10 + 1, 300 + 1);
                    graphics.drawString(formatter.format(team1.getVotePercent() * 100) + "%", 10 + 1, 300 - 1);
                    graphics.drawString(formatter.format(team1.getVotePercent() * 100) + "%", 10 - 1, 300 + 1);
                    graphics.drawString(formatter.format(team1.getVotePercent() * 100) + "%", 10 - 1, 300 - 1);
                } else {
                    graphics.drawString("0.0%", 10 + 1, 300 + 1);
                    graphics.drawString("0.0%", 10 + 1, 300 - 1);
                    graphics.drawString("0.0%", 10 - 1, 300 + 1);
                    graphics.drawString("0.0%", 10 - 1, 300 - 1);
                }
                if (team2.getVotePercent() != null) {
                    graphics.drawString(formatter.format(team2.getVotePercent() * 100) + "%", 695 + 1, 300 + 1);
                    graphics.drawString(formatter.format(team2.getVotePercent() * 100) + "%", 695 + 1, 300 - 1);
                    graphics.drawString(formatter.format(team2.getVotePercent() * 100) + "%", 695 - 1, 300 + 1);
                    graphics.drawString(formatter.format(team2.getVotePercent() * 100) + "%", 695 - 1, 300 - 1);
                } else {
                    graphics.drawString("0.0%", 695 + 1, 300 + 1);
                    graphics.drawString("0.0%", 695 + 1, 300 - 1);
                    graphics.drawString("0.0%", 695 - 1, 300 + 1);
                    graphics.drawString("0.0%", 695 - 1, 300 - 1);
                }
                graphics.setColor(new Color(103, 58, 183));
                
                if (team1.getVotePercent() != null) {
                    graphics.drawString(formatter.format(team1.getVotePercent() * 100) + "%", 10, 300);
                } else {
                    graphics.drawString("0.0%", 10, 300);
                }
                if (team2.getVotePercent() != null) {
                    graphics.drawString(formatter.format(team2.getVotePercent() * 100) + "%", 695, 300);
                } else {
                    graphics.drawString("0.0%", 695, 300);
                }
            }
            graphics.dispose();
            // File outputFile = new File(this.getClass().getResource("/").getPath() + "/static/stats_" + group.getId() + "_" + matchId + ".png");
            // ImageIO.write(templateImage, "png", outputFile);
            InputStream inputStream = writeToInputStream(templateImage, "png");
            DiscordListener.sendStats(group.getId(), competitionId, inputStream, isFuture);
        } catch (FontFormatException | IOException | URISyntaxException e) {
            e.printStackTrace();
        }
    }
    
    private static InputStream writeToInputStream(BufferedImage image, String format) throws IOException {
        // Write the BufferedImage to a ByteArrayOutputStream
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(image, format, outputStream);
        
        // Convert the ByteArrayOutputStream to a ByteArrayInputStream
        return new ByteArrayInputStream(outputStream.toByteArray());
    }
    
    @Override
    public List<User> getUsersThatForgotToPronoTheDay(PronoWeek week, PronoDay day) {
        Optional<List<Pronostic>> userPronosForWeekOpt = this.pronoRepository.findByWeekId(week.getId());
        List<User> usersThatDidNotPronoMadge = this.userService.getAllUsersWithReminder();
        if (userPronosForWeekOpt.isPresent()) {
            List<Pronostic> userPronosForWeek = userPronosForWeekOpt.get();
            for(Pronostic pronostic : userPronosForWeek) {
                List<MatchScore> dayScores = pronostic.getScores().stream().filter(score -> day.getMatchs().stream().anyMatch(m -> m.getId().equals(score.getMatchId()))).toList();
                boolean allgood = true;
                if (dayScores == null || dayScores.isEmpty()) {
                    allgood = false;
                } else {
                    for(MatchScore score : dayScores) {
                        if (score.getWinner() == null || "".equals(score.getWinner())) {
                            allgood = false;
                        }
                    }
                }
                if (allgood) {
                    usersThatDidNotPronoMadge.removeIf(u -> u.getUserId().equals(pronostic.getUserId()));
                }
            }
        }
        return usersThatDidNotPronoMadge;
    }
    
    @Override
    public List<Traitor> findTheTraitors(String groupName, String belovedTeam, String name, String year, String split) {
        Competition competition = competitionRepository.findByNameAndYearAndSplit(name, year, split).orElse(null);
        Group group = groupService.getGroupByName(groupName);
        if (competition != null && group != null) {
            List<PronoWeek> weeksWithTeam = pronoWeekRepository.findByCompetitionIdAndPronoDaysMatchsTeamsCode(competition.getId(), belovedTeam);
            List<String> matchsWithTeam = new ArrayList<>();
            
            weeksWithTeam.forEach(week -> {
                week.getPronoDays().forEach(day -> {
                    day.getMatchs().forEach(match -> {
                        if (match.getTeams().stream().anyMatch(t -> t.code.equals(belovedTeam))) {
                            matchsWithTeam.add(match.getId());
                        }
                    });
                });
            });
            
            List<Pronostic> allPronoForMatchList = pronoRepository.findByGroupIdAndScoresMatchIdIn(group.getId(), matchsWithTeam).orElse(null);
            Map<String, Traitor> allTraitors = new HashMap<>();
            allPronoForMatchList.forEach(prono -> {
                prono.getScores().forEach(score -> {
                    if (!allTraitors.containsKey(prono.getUserId())) {
                        Traitor traitor = new Traitor();
                        traitor.setUserId(prono.getUserId());
                        allTraitors.put(prono.getUserId(), traitor);
                    }
                    if (matchsWithTeam.contains(score.getMatchId())) {
                        allTraitors.get(prono.getUserId()).addMatch();
                        if (!belovedTeam.equals(score.getWinner())) {
                            allTraitors.get(prono.getUserId()).addTreachery();
                        }
                    }
                });
            });
            // not a traitor after all, saved
            allTraitors.entrySet().removeIf(traitor -> traitor.getValue().getNumberOfTreacheries() == 0);
            
            List<Traitor> traitors = new ArrayList<>(allTraitors.values());
            traitors.forEach(traitor -> {
                User user = userService.getUserByUserId(traitor.getUserId());
                traitor.setDisplayName(user.getDisplayName());
            });
            traitors.sort((t1, t2) -> Integer.compare(t2.getNumberOfTreacheries(), t1.getNumberOfTreacheries()));
            return traitors;
        }
        return null;
    }
}
