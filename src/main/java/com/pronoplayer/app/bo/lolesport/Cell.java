package com.pronoplayer.app.bo.lolesport;

import java.util.List;

import lombok.Getter;

@Getter
public class Cell{
    public String name;
    public String slug;
    public List<Match> matches;
}

