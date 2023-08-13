package com.pronoplayer.app.prono;

import java.util.List;
import java.util.Map;

import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;
import com.pronoplayer.app.bo.twitch.TwitchValidation;
import com.pronoplayer.app.twitch.TwitchService;

import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@RestController
public class PronoControllerImpl implements PronoController {
    
    private final PronoService pronoService;
    private final TwitchService twitchService;
    
    @Override
    public PronoWeek getPronoByCompetitionIdAndWeek(String competitionId, String week, String period) {
        return pronoService.getPronoByCompetitionIdAndWeek(competitionId, week, period);
    }
    
    @Override
    public PronoWeek getPronoByCompetitionIdAndDate(String competitionId, String date) {
        return pronoService.getPronoByCompetitionIdAndDate(competitionId, date);
    }
    
    @Override
    public Pronostic getPronoForUser(String competitionId, String groupId, String weekId, String userId) {
        return pronoService.getPronoForUser(competitionId, groupId, weekId, userId);
    }
    
    @Override
    public List<PronoWeek> getPronoWeeksByCompetitionId(String competitionId) {
        return pronoService.getPronoWeeksByCompetitionId(competitionId);
    }
    
    @Override
    public ResponseEntity<Pronostic> updateScores(String userId, Pronostic pronostic, String token, String refreshToken) {
        TwitchValidation validation = twitchService.validateToken(token, refreshToken);
        if (validation != null && validation.getUserId().equals(userId)) {  
            Pronostic savedProno = pronoService.updateScores(userId, pronostic);
            HttpHeaders headers = new HttpHeaders();
            if (validation.getRenewedToken() != null) {
                headers.add("access_token", validation.getRenewedToken().getAccessToken());
                headers.add("refresh_token", validation.getRenewedToken().getRefreshToken());
            }
            return new ResponseEntity<Pronostic>(savedProno, headers, HttpStatus.OK);
        }
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(null);
    }
    
    @Override
    public Map<String, Integer> getRanking(String groupId,  String competitionId) {
        return pronoService.getRanking(groupId, competitionId);
    }
}
