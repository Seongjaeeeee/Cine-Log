package com.cinelog.server.repository.jdbc;

import java.util.List;
import java.util.Optional;
import java.util.Map;
import java.util.ArrayList;

import javax.sql.DataSource;

import com.cinelog.server.domain.Review;
import com.cinelog.server.domain.User;
import com.cinelog.server.domain.Movie;
import com.cinelog.server.domain.Director; // 추가
import com.cinelog.server.repository.ReviewRepository;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;

public class ReviewJdbcRepository implements ReviewRepository {
    
    private final SimpleJdbcInsert jdbcInsert;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    public ReviewJdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("reviews")
                .usingGeneratedKeyColumns("id");
    }
    @Override
    public Review save(Review review) {
        if (review.getId() == null) {
            return insert(review);
        } else {
            return update(review);
        }
    }
    @Override
    public Optional<Review> findById(Long id) {
        // d.id와 d.name 조인 추가
        String sql = "SELECT r.*, m.name as movie_name, m.director_id, d.name as director_name, u.user_name " +
                     "FROM reviews r " +
                     "JOIN movies m ON r.movie_id = m.id " +
                     "JOIN directors d ON m.director_id = d.id " + // 감독 조인
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.id = :id";
        try {
            return Optional.ofNullable(jdbcTemplate.queryForObject(sql, Map.of("id", id), reviewRowMapper()));
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    @Override
    public List<Review> findByMovieId(Long movieId) {
        String sql = "SELECT r.*, m.name as movie_name, m.director_id, d.name as director_name, u.user_name " +
                     "FROM reviews r " +
                     "JOIN movies m ON r.movie_id = m.id " +
                     "JOIN directors d ON m.director_id = d.id " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.movie_id = :movieId";
        return jdbcTemplate.query(sql, Map.of("movieId", movieId), reviewRowMapper());
    }

    @Override
    public List<Review> findByUser(User user) {
        String sql = "SELECT r.*, m.name as movie_name, m.director_id, d.name as director_name, u.user_name " +
                     "FROM reviews r " +
                     "JOIN movies m ON r.movie_id = m.id " +
                     "JOIN directors d ON m.director_id = d.id " +
                     "JOIN users u ON r.user_id = u.id " +
                     "WHERE r.user_id = :userId";
        return jdbcTemplate.query(sql, Map.of("userId", user.getId()), reviewRowMapper());
    }
    @Override
    public boolean delete(Long id) {
        String sql = "DELETE FROM reviews WHERE id = :id";
        int affectedRows = jdbcTemplate.update(sql, Map.of("id", id));
        return affectedRows > 0;
    }

    private Review insert(Review review) {
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("content", review.getContent())
                .addValue("rating", review.getRating())
                .addValue("movie_id", review.getMovie().getId())
                .addValue("user_id", review.getUser().getId());
        Number key = jdbcInsert.executeAndReturnKey(params);
        review.setId(key.longValue());
        return review;
    }

    private Review update(Review review) {
        String sql = "UPDATE reviews SET content = :content, rating = :rating WHERE id = :id";
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", review.getId())
                .addValue("content", review.getContent())
                .addValue("rating", review.getRating());
        int affectedRows = jdbcTemplate.update(sql, params);
        if (affectedRows == 0) throw new RuntimeException("해당 리뷰를 찾을 수 없습니다. ID: " + review.getId());
        
        return review;
    }

    private RowMapper<Review> reviewRowMapper() {
        return (rs, rowNum) -> {
            User user = new User(rs.getString("user_name"), "PROTECTED_PASSWORD");
            user.setId(rs.getLong("user_id"));
            Director director = new Director(rs.getString("director_name"));
            director.setId(rs.getLong("director_id"));
            Movie movie = new Movie(
                rs.getString("movie_name"), 
                director, // 더 이상 null이 아님
                null, 
                null, 
                null, 
                new ArrayList<>() 
            );
            movie.setId(rs.getLong("movie_id"));
            
            //Review 객체 복원 (reconstitute 사용하여 addRating 부작용 방지)
            return Review.reconstitute(
                rs.getLong("id"),
                rs.getString("content"),
                rs.getInt("rating"),
                user,
                movie
            );  
        };
    }
}