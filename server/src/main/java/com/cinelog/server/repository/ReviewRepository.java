package com.cinelog.server.repository;

import java.util.List;
import java.util.Optional;

import com.cinelog.server.domain.Review;
import com.cinelog.server.domain.User;

public interface ReviewRepository {
    public Review save(Review review);
    public Optional<Review> findById(Long id);
    public List<Review> findByMovieId(Long id);
    public List<Review> findByUser(User user);
    public boolean delete(Long id);
}
