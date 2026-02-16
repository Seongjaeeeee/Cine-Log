package com.cinelog.server.dto.movie;

import java.util.List;

import com.cinelog.server.dto.actor.ActorSearchResult;
import com.cinelog.server.dto.director.DirectorSearchResult;

import lombok.Value;

@Value
public class PersonSearchResult {
    private List<ActorSearchResult> actors;
    private List<DirectorSearchResult> directors;
}
