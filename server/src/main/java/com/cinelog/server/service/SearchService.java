package com.cinelog.server.service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.cinelog.server.dto.movie.MovieSearchResult;
import com.cinelog.server.dto.movie.PersonSearchResult;

@Service
public class SearchService {
    private final MovieService movieService;
    private final ActorService actorService;
    private final DirectorService directorService;

    public SearchService(MovieService movieService, ActorService actorService,DirectorService directorService){
        this.movieService = movieService;
        this.actorService = actorService;
        this.directorService = directorService;
    }
    
    public List<MovieSearchResult> searchAllMovie(String keyword){
        List<MovieSearchResult> results = new ArrayList<>(); 
        results.addAll(movieService.findAllMoviesByKeyword(keyword));
        results.addAll(movieService.findAllMoviesByActorKeyword(keyword));//관련된 배우가 들어간 영화 반환
        results.addAll(movieService.findAllMoviesByDirectorKeyword(keyword));//관련된 감독이 들어간 영화 반환
        
        List<MovieSearchResult> uniqueResults = results.stream().distinct().collect(Collectors.toList());
        
        return uniqueResults;
    }

    public PersonSearchResult searchPerson(String keyword){
        PersonSearchResult results = 
        new PersonSearchResult(actorService.findAllActorsByKeyword(keyword),directorService.findAllDirectorsByKeyword(keyword));
        return results;
    }
}
