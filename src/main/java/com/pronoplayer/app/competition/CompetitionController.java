package com.pronoplayer.app.competition;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.pronoplayer.app.bo.Competition;
import com.pronoplayer.app.bo.lolesport.Standing;

@RestController
@RequestMapping(value = "/api/competition")
public interface CompetitionController {
    
    @GetMapping("/currents")
    public List<Competition> findAllCurrents();
    
    @GetMapping("/ids")
    public List<Competition> findAllByIds(@RequestParam List<String> ids);
        
    @GetMapping("/{id}")
    public Competition findById(@PathVariable("id") String id);

    @GetMapping("/{id}/standings")
    public Standing getStanding(@PathVariable("id") String competitionId);
    
    @GetMapping("/load")
    public void loadFromLolesports();
}
