package com.cinelog.server.domain;

import java.util.Map;

public class BasicRatingPolicy implements RatingPolicy{
    public double calculateRating(Map<Integer, Long> ratingDistribution){
        long count=0;
        long totalsum=0;
        for(int i=1;i<=5;i++){
            long curCount = ratingDistribution.getOrDefault(i, 0L);
            totalsum+=i*curCount;
            count+=curCount;
        }
        if (count == 0) {
            return 0.0;
        }
        return (double)totalsum/count;
    } 
}
