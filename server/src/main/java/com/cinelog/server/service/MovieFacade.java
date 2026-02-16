package com.cinelog.server.service;

import java.time.LocalDate;
import java.util.ArrayList;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;

@Service
public class MovieFacade {
    private final MovieService movieService;
    private final ActorService actorService;
    private final DirectorService directorService;
    public MovieFacade(MovieService movieService,ActorService actorService,DirectorService directorService){
        this.movieService = movieService;
        this.actorService = actorService;
        this.directorService = directorService;
    }
    //객체 조립이 필요한 메서드들
    @Transactional
    public void createMovie(String name, Genre genre, LocalDate releaseDate, String description,Long directorId,Long ... actorIds){
        Director director = directorService.getDirectorById(directorId);
        ArrayList<Actor> actors=new ArrayList<>();
        for(Long actorId: actorIds){
            actors.add(actorService.getActorById(actorId));
        }
        movieService.createMovie(name, director, genre, releaseDate, description, actors);
    }
    @Transactional
    public void updateMovieDirector(Long movieId, Long directorId){
        Movie movie = movieService.getMovieById(movieId);
        Director director = directorService.getDirectorById(directorId);
        movieService.updateMovieDirector(movie,director);
    }
    @Transactional
    public void removeActorFromMovie(Long movieId,Long actorId){
        Movie movie = movieService.getMovieById(movieId);
        Actor actor = actorService.getActorById(actorId);
        movieService.removeActorFromMovie(movie,actor);
    }
    @Transactional
    public void addActorToMovie(Long movieId,Long actorId){
        Movie movie = movieService.getMovieById(movieId);
        Actor actor = actorService.getActorById(actorId);
        movieService.addActorToMovie(movie,actor);
    }
}
