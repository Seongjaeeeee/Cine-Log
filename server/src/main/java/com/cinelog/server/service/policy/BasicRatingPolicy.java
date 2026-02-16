package com.cinelog.server.service.policy;

import org.springframework.stereotype.Component;

import com.cinelog.server.domain.RatingPolicy;
import com.cinelog.server.repository.ReviewRepository;

@Component
public class BasicRatingPolicy implements RatingPolicy{
    private final ReviewRepository reviewRepository;
    public BasicRatingPolicy(ReviewRepository reviewRepository){
        this.reviewRepository = reviewRepository;
    }
    public double calculateRating(Long movieId){
        return reviewRepository.calculateAverageRatingByMovieId(movieId);
    }
}
