package com.cinelog.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;

import com.cinelog.server.domain.Movie;
import com.cinelog.server.domain.Review;
import com.cinelog.server.domain.User;
import com.cinelog.server.exception.security.ForbiddenException;
import com.cinelog.server.repository.ReviewRepository;
import com.cinelog.server.domain.event.ReviewChangedEvent;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;
    @Mock
    private MovieService movieService;
    @Mock
    private UserService userService;
    @Mock
    private ApplicationEventPublisher eventPublisher;
    @InjectMocks
    private ReviewService reviewService;

    @Test
    @DisplayName("리뷰 생성 성공: 리뷰를 저장하고, 평점 정책을 호출하여 영화 평점을 갱신한다")
    void createReview_Success() {
        // Given
        Long userId = 1L;
        Long movieId = 10L;
        String content = "최고의 영화";
        Integer rating = 5;
        User mockUser = mock(User.class);
        Movie mockMovie = mock(Movie.class);

        // 1. 중복 체크 통과
        given(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).willReturn(false);
        // 2. 유저와 영화 조회 성공
        given(userService.getUserById(userId)).willReturn(mockUser);
        given(movieService.getMovieById(movieId)).willReturn(mockMovie);

        // When
        reviewService.createReview(content, rating, userId, movieId);

        // Then
        verify(reviewRepository).save(any(Review.class));
        verify(eventPublisher).publishEvent(any(ReviewChangedEvent.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패: 이미 리뷰를 작성한 유저는 중복 작성할 수 없다")
    void createReview_Fail_Duplicate() {
        // Given
        Long userId = 1L;
        Long movieId = 10L;

        given(reviewRepository.existsByUserIdAndMovieId(userId, movieId)).willReturn(true);

        // When & Then
        assertThatThrownBy(() -> 
            reviewService.createReview("내용", 5, userId, movieId)
        ).isInstanceOf(IllegalArgumentException.class)
         .hasMessage("이미 해당 영화에 대한 리뷰를 작성했습니다.");

        // 검증: 저장이나 이벤트 발생이 실행되면 안 됨
        verify(reviewRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }

    
    @Test
    @DisplayName("유저별 리뷰 조회: 레포지토리 결과를 그대로 반환한다")
    void findReviewByUserId_Success() {
        // Given
        Long userId = 1L;
        List<Review> mockList = List.of(mock(Review.class), mock(Review.class));
        given(reviewRepository.findByUserId(userId)).willReturn(mockList);

        // When
        List<Review> result = reviewService.findReviewByUserId(userId);

        // Then
        assertThat(result).hasSize(2);
        verify(reviewRepository).findByUserId(userId);
    }

    @Test
    @DisplayName("영화별 리뷰 조회: 레포지토리 결과를 그대로 반환한다")
    void findReviewByMovieId_Success() {
        // Given
        Long movieId = 10L;
        List<Review> mockList = List.of(mock(Review.class), mock(Review.class));
        given(reviewRepository.findByMovieId(movieId)).willReturn(mockList);

        // When
        List<Review> result = reviewService.findReviewByMovieId(movieId);

        // Then
        assertThat(result).hasSize(2);
        verify(reviewRepository).findByMovieId(movieId);
    }

    @Test
    @DisplayName("리뷰 수정 성공: 작성자 본인은 리뷰를 수정할 수 있다")
    void updateReview_Success() {
        // Given
        Long reviewId = 100L;
        Long userId = 1L;
        Long movieId = 10L;
        String newContent = "수정된 내용";
        Integer newRating = 4;

        User owner = mock(User.class); // 작성자
        Review review = mock(Review.class); // 수정 대상 리뷰

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userService.getUserById(userId)).willReturn(owner);
        given(review.isOwner(owner)).willReturn(true);
        given(review.getMovieId()).willReturn(movieId);

        // When
        reviewService.updateReview(newContent, newRating, reviewId, userId);

        // Then
        verify(review).update(newContent, newRating); 
        verify(reviewRepository).save(review); 
        
        ArgumentCaptor<ReviewChangedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        
        ReviewChangedEvent capturedEvent = eventCaptor.getValue();
        assertThat(capturedEvent.getMovieId()).isEqualTo(movieId);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 작성자가 아닌 유저는 수정할 수 없다 (Forbidden)")
    void updateReview_Fail_NotOwner() {
        // Given
        Long reviewId = 100L;
        Long userId = 2L; // 다른 유저 (해커)

        User otherUser = mock(User.class);
        Review review = mock(Review.class);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userService.getUserById(userId)).willReturn(otherUser);
        given(review.isOwner(otherUser)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> 
            reviewService.updateReview("해킹 시도", 1, reviewId, userId)
        ).isInstanceOf(ForbiddenException.class);

        verify(review, never()).update(any(), any());
        verify(reviewRepository, never()).save(any());
        verify(eventPublisher, never()).publishEvent(any());
    }


    @Test
    @DisplayName("리뷰 수정 실패: 존재하지 않는 리뷰는 수정할 수 없다")
    void updateReview_Fail_NotFound() {
        // Given
        Long invalidReviewId = 999L;
        given(reviewRepository.findById(invalidReviewId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> 
            reviewService.updateReview("내용", 5, invalidReviewId, 1L)
        ).isInstanceOf(IllegalArgumentException.class);
    }

    // --- 3. 리뷰 삭제 (Delete) ---

    @Test
    @DisplayName("리뷰 삭제 성공: 작성자 본인은 리뷰를 삭제할 수 있다")
    void deleteReview_Success() {
        // Given
        Long reviewId = 100L;
        Long userId = 1L;
        Long movieId = 10L;

        User owner = mock(User.class);
        Review review = mock(Review.class);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userService.getUserById(userId)).willReturn(owner);
        given(review.isOwner(owner)).willReturn(true);
        given(review.getMovieId()).willReturn(movieId);

        // When
        reviewService.deleteReview(reviewId, userId);

        // Then
        verify(reviewRepository).delete(reviewId);
        
        ArgumentCaptor<ReviewChangedEvent> eventCaptor = ArgumentCaptor.forClass(ReviewChangedEvent.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue().getMovieId()).isEqualTo(movieId);
    }

    @Test
    @DisplayName("리뷰 삭제 실패: 작성자가 아닌 유저는 삭제할 수 없다")
    void deleteReview_Fail_NotOwner() {
        // Given
        Long reviewId = 100L;
        Long userId = 2L;

        User otherUser = mock(User.class);
        Review review = mock(Review.class);

        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));
        given(userService.getUserById(userId)).willReturn(otherUser);
        given(review.isOwner(otherUser)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> 
            reviewService.deleteReview(reviewId, userId)
        ).isInstanceOf(ForbiddenException.class);

        verify(eventPublisher, never()).publishEvent(any());
    }

}