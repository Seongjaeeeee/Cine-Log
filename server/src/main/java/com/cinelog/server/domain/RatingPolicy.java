package com.cinelog.server.domain;

import java.util.Map;
public interface RatingPolicy {
    public double calculateRating(Map<Integer, Long> ratingDistribution);
}