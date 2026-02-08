package com.cinelog.server.repository.jdbc;

import com.cinelog.server.domain.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import({
    ReviewJdbcRepository.class, 
    MovieJdbcRepository.class, 
    UserJdbcRepository.class, 
    DirectorjdbcRepository.class
})
class ReviewJdbcRepositoryTest {

    @Autowired private ReviewJdbcRepository reviewRepository;
  
    @Autowired private MovieJdbcRepository movieRepository;
    @Autowired private UserJdbcRepository userRepository;
    @Autowired private DirectorjdbcRepository directorRepository;
    
    private User testUser;
    private Movie testMovie;

    @BeforeEach
    void setUp() {// 리뷰 저장을 위해 필수적인 User와 Movie를 미리 저장합니다.
        testUser = userRepository.save(new User("tester", "password123"));
        
        Director director = directorRepository.save(new Director("봉준호"));
        testMovie = movieRepository.save(new Movie(
            "기생충", director, Genre.DRAMA, LocalDate.now(), "설명", new ArrayList<>()
        ));
    }

    @Test
    @DisplayName("리뷰 저장 시 DB에서 다시 조회했을 때 내용과 외래키 ID가 정확히 일치해야 한다")
    void saveInsertTest() {
        // Given
        Review review = Review.create("정말 재밌어요!", 5, testUser, testMovie);

        // When
        Review saved = reviewRepository.save(review);

        // Then
        Review found = reviewRepository.findById(saved.getId())
                .orElseThrow(() -> new AssertionError("리뷰가 DB에 저장되지 않음"));

        assertThat(found.getContent()).isEqualTo("정말 재밌어요!");
        assertThat(found.getRating()).isEqualTo(5);
        assertThat(found.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.getMovie().getId()).isEqualTo(testMovie.getId());
    }

    @Test
    @DisplayName("리뷰 수정 시 내용과 별점이 DB에 실제로 반영되어야 한다")
    void saveUpdateTest() {
        // Given
        Review saved = reviewRepository.save(Review.create("최고!", 5, testUser, testMovie));
        saved.update("생각해보니 보통이네요", 3);

        // When
        reviewRepository.save(saved);

        // Then
        Review updated = reviewRepository.findById(saved.getId()).get();
        assertThat(updated.getContent()).isEqualTo("생각해보니 보통이네요");
        assertThat(updated.getRating()).isEqualTo(3);
    }
    @Test
    @DisplayName("리뷰 ID로 조회 시 조인을 통해 영화 제목과 유저 이름까지 정확히 복원되어야 한다")
    void findByIdSuccessTest() {
        //Given
        Review review = Review.create("영화가 너무 슬퍼요..", 4, testUser, testMovie);
        Review saved = reviewRepository.save(review);
        Long savedId = saved.getId();

        // When
        Optional<Review> foundOptional = reviewRepository.findById(savedId);

        // Then
        assertThat(foundOptional).isPresent();
        Review found = foundOptional.get();

        assertThat(found.getId()).isEqualTo(savedId);
        assertThat(found.getContent()).isEqualTo("영화가 너무 슬퍼요..");
        assertThat(found.getRating()).isEqualTo(4);

        assertThat(found.getUser().getId()).isEqualTo(testUser.getId());
        assertThat(found.getUser().getName()).isEqualTo("tester"); // SQL에서 가져온 user_name

        assertThat(found.getMovie().getId()).isEqualTo(testMovie.getId());
        assertThat(found.getMovie().getName()).isEqualTo("기생충"); // SQL에서 가져온 movie_name
    }
    @Test
    @DisplayName("존재하지 않는 ID로 조회하면 Optional.empty()를 반환해야 한다")
    void findById_Empty() {
        assertThat(reviewRepository.findById(99999L)).isEmpty();
    }
    @Test
    @DisplayName("특정 영화의 ID로 해당 영화에 달린 모든 리뷰를 가져와야 한다")
    void findByMovieIdTest() {
        // Given
        reviewRepository.save(Review.create("리뷰1", 4, testUser, testMovie));
        reviewRepository.save(Review.create("리뷰2", 5, testUser, testMovie));

        // When
        List<Review> reviews = reviewRepository.findByMovieId(testMovie.getId());

        // Then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting("content").containsExactlyInAnyOrder("리뷰1", "리뷰2");
        // RowMapper 조인 확인: 영화 이름이 잘 들어왔는지
        assertThat(reviews.get(0).getMovie().getName()).isEqualTo("기생충");
    }

    @Test
    @DisplayName("특정 유저가 작성한 모든 리뷰를 조회해야 하며, 다른 유저의 리뷰는 포함되지 않아야 한다")
    void findByUserTest() {
        // Given
        User otherUser = userRepository.save(new User("other_user", "password"));
        reviewRepository.save(Review.create("너무 재밌어요!", 5, testUser, testMovie));
        reviewRepository.save(Review.create("또 보고 싶네요.", 4, testUser, testMovie));
        reviewRepository.save(Review.create("그냥 그래요.", 2, otherUser, testMovie));

        // When
        List<Review> reviews = reviewRepository.findByUser(testUser);

        // Then
        assertThat(reviews).hasSize(2);
        assertThat(reviews).extracting("content")
                .containsExactlyInAnyOrder("너무 재밌어요!", "또 보고 싶네요.");
        assertThat(reviews).allSatisfy(review -> {
            assertThat(review.getUser().getName()).isEqualTo("tester");
            assertThat(review.getMovie().getName()).isEqualTo("기생충");
        });
    }

    @Test
    @DisplayName("리뷰 삭제 시 DB 테이블에서 해당 로우가 완전히 제거되어야 한다")
    void deleteTest() {
        // Given
        Review saved = reviewRepository.save(Review.create("지울 리뷰", 1, testUser, testMovie));
        Long id = saved.getId();

        // When
        boolean isDeleted = reviewRepository.delete(id);

        // Then
        assertThat(isDeleted).isTrue();
        assertThat(reviewRepository.findById(id)).isEmpty();
    }
    @Test
    @DisplayName("존재하지 않는 리뷰를 삭제하려 하면 false를 반환해야 한다")
    void delete_Fail() {
        boolean result = reviewRepository.delete(99999L);
        assertThat(result).isFalse();
    }
}