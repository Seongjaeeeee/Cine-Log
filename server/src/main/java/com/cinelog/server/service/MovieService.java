package com.cinelog.server.service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;
import com.cinelog.server.dto.movie.MovieSearchResult;
import com.cinelog.server.dto.movie.MovieUpdateRequest;
import com.cinelog.server.exception.movie.MovieNotFoundException;
import com.cinelog.server.repository.MovieRepository;

@Service
@Transactional(readOnly = true)
public class MovieService {
    
    private final MovieRepository movieRepository;
    
    public MovieService(MovieRepository movieRepository){
        this.movieRepository = movieRepository;
    }

    @Transactional
    public void createMovie(String name, Director director, Genre genre, LocalDate releaseDate, String description,List<Actor> actors){
        Movie movie = new Movie(name,director,genre,releaseDate,description,actors);
        movieRepository.save(movie);
    }//이미 존재하는 영화인지 다른 식별자를 이용해 체크하고 입력하는 방안 고민
   
    public Movie getMovieById(Long id){
        return movieRepository.findById(id).orElseThrow(()->new MovieNotFoundException(id));
    }
    public List<Movie> findAllMovies(){
        return movieRepository.findAll();
    }
    public List<MovieSearchResult> findAllMoviesByKeyword(String keyword){
        return toMovieSearchResult(movieRepository.findAllByNameContaining(keyword));
    }
    public List<MovieSearchResult> findAllMoviesByActorKeyword(String keyword){
        return toMovieSearchResult(movieRepository.findAllByActorNameContaining(keyword));
    }
    public List<MovieSearchResult> findAllMoviesByDirectorKeyword(String keyword){
        return toMovieSearchResult(movieRepository.findAllByDirectorNameContaining(keyword));
    }

    @Transactional
    public void updateMovieInfo(Long id,MovieUpdateRequest request){
        Movie movie = getMovieById(id);
        movie.updateMovieInfo(request.getName(), request.getGenre(), request.getReleaseDate(), request.getDescription());
        movieRepository.save(movie);
    }
    @Transactional
    public void updateMovieDirector(Movie movie, Director director) {
        movie.changeDirector(director);
        movieRepository.save(movie);
    }
    @Transactional
    public void removeActorFromMovie(Movie movie, Actor actor) {
        movie.removeActor(actor);
        movieRepository.save(movie);
    }
    @Transactional
    public void addActorToMovie(Movie movie, Actor actor) {
        movie.addActor(actor);
        movieRepository.save(movie);
    }
    @Transactional
    public void updateMovieRating(Movie movie, Double rating){
        movie.updateRating(rating);
        movieRepository.save(movie);
    }

    @Transactional
    public void deleteMovie(Long id){
        if(!movieRepository.delete(id))throw new MovieNotFoundException(id);
    }
    
    private List<MovieSearchResult> toMovieSearchResult(List<Movie> movies){
        List<MovieSearchResult> results= new ArrayList<>();
        for(Movie movie : movies){
            results.add(new MovieSearchResult(movie.getName(), movie.getId()));
        }
        return results;
    }

}
