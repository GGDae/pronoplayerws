package com.pronoplayer.app.prono;

import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.PronoWeek;
import com.pronoplayer.app.bo.Pronostic;

@RestController
@RequestMapping(value = "/api/prono")
public interface PronoController {
    
    @GetMapping("/week/{week}")
    public PronoWeek getPronoByCompetitionIdAndWeek(@RequestParam String competitionId, @PathVariable("week") String week, @RequestParam String period);
    
    @GetMapping("/schedule/{date}")
    public PronoWeek getPronoByCompetitionIdAndDate(@RequestParam String competitionId, @PathVariable("date") String date);

    @GetMapping("/schedule")
    public List<PronoWeek> getPronoWeeksByCompetitionId(@RequestParam String competitionId);

    @GetMapping("/{userId}")
    public Pronostic getPronoForUser(@RequestParam String competitionId, @RequestParam String groupId, @RequestParam String weekId, @PathVariable("userId") String userId);

    @PatchMapping("/{userId}")
    public ResponseEntity<Pronostic> updateScores(@PathVariable("userId") String userId, @RequestBody Pronostic pronostic, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/{competitionId}/snapshot")
    public ResponseEntity<Void> generateRankingImage(@PathVariable("groupId") String groupId, @PathVariable("competitionId") String competitionId, @RequestHeader("Authorization") String token, @RequestHeader("refreshtoken") String refreshToken);

    @GetMapping("/{groupId}/ranking")
    public Map<String, Integer[]> getRanking(@PathVariable("groupId") String groupId, @RequestParam String competitionId);

}
