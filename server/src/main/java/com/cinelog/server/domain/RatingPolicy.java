package com.cinelog.server.domain;

public interface RatingPolicy {
    public double calculateRating(Long movieId);
}