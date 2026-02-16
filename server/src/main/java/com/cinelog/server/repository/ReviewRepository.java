package com.cinelog.server.repository;

import java.util.List;
import java.util.Optional;

import com.cinelog.server.domain.Review;

public interface ReviewRepository {
    public Review save(Review review);
    public Optional<Review> findById(Long id);
    public List<Review> findByMovieId(Long id);
    public List<Review> findByUserId(Long id);
    public boolean existsByUserIdAndMovieId(Long userId,Long movieId);
    public boolean delete(Long id);
    public Double calculateAverageRatingByMovieId(Long movieId);
}
