package com.cinelog.server.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Movie;
import com.cinelog.server.domain.RatingPolicy;
import com.cinelog.server.domain.Review;
import com.cinelog.server.domain.User;
import com.cinelog.server.exception.security.ForbiddenException;
import com.cinelog.server.repository.ReviewRepository;

@Service
@Transactional(readOnly = true)
public class ReviewService {
    private final MovieService movieService;
    private final UserService userService;
    private final ReviewRepository reviewRepository;
    private RatingPolicy ratingPolicy;

    public ReviewService(ReviewRepository reviewRepository,MovieService movieService,UserService userService,RatingPolicy ratingPolicy){
        this.reviewRepository = reviewRepository;
        this.movieService = movieService;
        this.userService = userService;
        this.ratingPolicy = ratingPolicy;
    }

    @Transactional
    public void createReview(String content, Integer rating, Long userId, Long movieId) {
        if(reviewRepository.existsByUserIdAndMovieId(userId,movieId))throw new IllegalArgumentException("이미 해당 영화에 대한 리뷰를 작성했습니다.");
        User user = userService.getUserById(userId);
        Movie movie = movieService.getMovieById(movieId);
        Review review = Review.create(content,rating,user,movie);

        reviewRepository.save(review);

        movieService.updateMovieRating(movie,ratingPolicy.calculateRating(movieId));
    }
    
    public List<Review> findReviewByMovieId(Long movieId) {//영화 리뷰 보기
        return reviewRepository.findByMovieId(movieId);
    }
    public List<Review> findReviewByUserId(Long userId) {//내 리뷰 보기
        return reviewRepository.findByUserId(userId);
    }
    
    @Transactional
    public void updateReview(String content,Integer rating,Long reviewId,Long userId){
        Review review = getReviewById(reviewId);
        User user = userService.getUserById(userId);
        if(!isReviewOwner(user, review))throw new ForbiddenException("수정할 수 있는 유저가 아닙니다.");
        review.update(content,rating);
        reviewRepository.save(review);
    }
    @Transactional
    public void deleteReview(Long reviewId,Long userId){
        Review review = getReviewById(reviewId);
        User user = userService.getUserById(userId);
        if(!isReviewOwner(user, review))throw new ForbiddenException("삭제할 수 있는 유저가 아닙니다.");
        reviewRepository.delete(reviewId);
    }

    private boolean isReviewOwner(User user,Review review){
        return review.isOwner(user);
    }
    private Review getReviewById(Long reviewId){
        return reviewRepository.findById(reviewId).orElseThrow(()->new IllegalArgumentException("내 리뷰에 존재하지 않는 리뷰 id입니다."));
    }
    
}

