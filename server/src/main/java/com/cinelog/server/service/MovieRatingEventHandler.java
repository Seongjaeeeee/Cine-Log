package com.cinelog.server.service;

import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

import com.cinelog.server.domain.Movie;
import com.cinelog.server.domain.RatingPolicy;
import com.cinelog.server.domain.event.ReviewChangedEvent;

@Component
public class MovieRatingEventHandler{   
    private final MovieService movieService;
    private RatingPolicy ratingPolicy;

    MovieRatingEventHandler(MovieService movieService, RatingPolicy ratingPolicy){
        this.movieService = movieService;
        this.ratingPolicy = ratingPolicy;
    }

    @EventListener
    public void handleReviewChangedEvent(ReviewChangedEvent event) {
        Long movieId = event.getMovieId();
        Movie movie = movieService.getMovieById(movieId);

        Double newRating = ratingPolicy.calculateRating(movieId);
        movieService.updateMovieRating(movie, newRating);
    }
}