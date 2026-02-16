package com.cinelog.server.repository.jdbc;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.ResultSetExtractor;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;
import com.cinelog.server.repository.MovieRepository;

@Repository
public class MovieJdbcRepository implements MovieRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public MovieJdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                .withTableName("movies")
                .usingGeneratedKeyColumns("id");
    }

    @Override
    @Transactional
	public Movie save(Movie movie){
        if (movie.getId() == null) {
            return insert(movie);
        }
        return update(movie);
    }
    @Override
	public Optional<Movie> findById(Long id){
        String sql = "SELECT m.*, d.name as director_name " +
                     "FROM movies m " +
                     "JOIN directors d ON m.director_id = d.id " +
                     "WHERE m.id = :id";
        try{
            Movie movie = jdbcTemplate.queryForObject(sql,Map.of("id",id),movieMapper());
            if (movie != null) {
                movie.getActors().addAll(findActorsByMovieId(id));//배우 목록은 db에 저장되어있지 않으므로 따로처리
            }
            return Optional.ofNullable(movie);
        }
        catch(EmptyResultDataAccessException e){
            return Optional.empty();
        }
    }
    @Override
    public List<Movie> findAll() {
        String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                    "FROM movies m " +
                    "JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                    "LEFT JOIN actors a ON ma.actor_id = a.id";

        return jdbcTemplate.query(sql, movieResultSetExtractor());
    }
    
    @Override
    public List<Movie> findAllByDirectorId(Long directorId) {
        String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                    "FROM movies m " +
                    "JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                    "LEFT JOIN actors a ON ma.actor_id = a.id " +
                    "WHERE m.director_id = :directorId"; // 특정 감독 필터링

        return jdbcTemplate.query(sql, Map.of("directorId", directorId), movieResultSetExtractor());
    }
    @Override
    public List<Movie> findAllByActorId(Long actorId) {
        String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                    "FROM movies m " +
                    "JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                    "LEFT JOIN actors a ON ma.actor_id = a.id " +
                    "WHERE m.id IN (SELECT movie_id FROM movie_actor WHERE actor_id = :actorId)";

        return jdbcTemplate.query(sql, Map.of("actorId", actorId), movieResultSetExtractor());
    }
	@Override
    public List<Movie> findAllByNameContaining(String keyword) {
        String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                    "FROM movies m " +
                    "JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                    "LEFT JOIN actors a ON ma.actor_id = a.id " +
                    "WHERE m.name LIKE :keyword"; // 영화 제목 필터링

        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql, Map.of("keyword", likeKeyword), movieResultSetExtractor());
    }
   
    @Override
    public List<Movie> findAllByDirectorNameContaining(String keyword) {
        String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                    "FROM movies m " +
                    "JOIN directors d ON m.director_id = d.id " +
                    "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                    "LEFT JOIN actors a ON ma.actor_id = a.id " +
                    "WHERE d.name LIKE :keyword"; // 감독 이름 필터링

        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql, Map.of("keyword", likeKeyword), movieResultSetExtractor());
    }  
    
    @Override
    public List<Movie> findAllByActorNameContaining(String keyword) {
    // 해당 키워드를 이름에 포함한 배우가 출연한 영화 ID들을 먼저 찾습니다.
    String sql = "SELECT m.*, d.name as director_name, a.id as actor_id, a.name as actor_name " +
                 "FROM movies m " +
                 "JOIN directors d ON m.director_id = d.id " +
                 "LEFT JOIN movie_actor ma ON m.id = ma.movie_id " +
                 "LEFT JOIN actors a ON ma.actor_id = a.id " +
                 "WHERE m.id IN (" +
                 "    SELECT ma_inner.movie_id " +
                 "    FROM movie_actor ma_inner " +
                 "    JOIN actors a_inner ON ma_inner.actor_id = a_inner.id " +
                 "    WHERE a_inner.name LIKE :keyword" +
                 ")";

        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql, Map.of("keyword", likeKeyword), movieResultSetExtractor());
    }

    @Override
    public Integer countByDirectorId(Long id){
        String sql = "SELECT count(*) FROM movies WHERE director_id = :id";
        Map<String, Object> params = Map.of("id", id);
        return jdbcTemplate.queryForObject(sql, params, Integer.class);
    }

	@Override
    @Transactional
    public boolean delete(Long id) {
        //실제 영화 데이터를 삭제 -> 관계테이블은 cascade설정
        String deleteMovieSql = "DELETE FROM movies WHERE id = :id";
        int affectedRows = jdbcTemplate.update(deleteMovieSql, Map.of("id", id));

        return affectedRows > 0;
    }

    private Movie insert(Movie movie) {
        SqlParameterSource params = new MapSqlParameterSource()//명시적으로 매핑
                .addValue("name", movie.getName())
                .addValue("director_id", movie.getDirector().getId())
                .addValue("genre", movie.getGenre().name()) // Enum을 String으로 저장
                .addValue("description", movie.getDescription())
                .addValue("release_date", movie.getReleaseDate())
                .addValue("rating", movie.getRating());

        Number key = jdbcInsert.executeAndReturnKey(params);
        movie.setId(key.longValue());

        saveRelatedEntities(movie);//배우와 영화사이 관계엔티티 레코드 작성
        return movie;
    }

    private Movie update(Movie movie) {
        String sql = "UPDATE movies SET " +
                     "name = :name, " +
                     "director_id = :directorId, " +
                     "genre = :genre, " +
                     "description = :description, " +
                     "release_date = :releaseDate, " +
                     "rating = :rating " +
                     "WHERE id = :id";

        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", movie.getId())
                .addValue("name", movie.getName())
                .addValue("directorId", movie.getDirector().getId())
                .addValue("genre", movie.getGenre().name())
                .addValue("description", movie.getDescription())
                .addValue("releaseDate", movie.getReleaseDate())
                .addValue("rating", movie.getRating());

        jdbcTemplate.update(sql, params);

        clearRelatedData(movie.getId());//관련된 관계엔티티 레코드 전부 삭제
        saveRelatedEntities(movie);//다시 관계 만들기
        return movie;
    }
    //영화-배우 관계
    private void saveRelatedEntities(Movie movie) {
        String actorSql = "INSERT INTO movie_actor (movie_id, actor_id) VALUES (:movieId, :actorId)";
        movie.getActors().forEach(actor -> {
            jdbcTemplate.update(actorSql, Map.of("movieId", movie.getId(), "actorId", actor.getId()));
        });
    }
    private void clearRelatedData(Long movieId) {
        String sql = "DELETE FROM movie_actor WHERE movie_id = :movieId";
        jdbcTemplate.update(sql, Map.of("movieId", movieId));
    }
    //read할때 영화-배우 관계
    private List<Actor> findActorsByMovieId(Long movieId) { 
        String sql = "SELECT a.* FROM actors a " +
                     "JOIN movie_actor ma ON a.id = ma.actor_id " +
                     "WHERE ma.movie_id = :movieId";
        return jdbcTemplate.query(sql, Map.of("movieId", movieId), (rs, rowNum) -> {
            Actor actor = new Actor(rs.getString("name"));
            actor.setId(rs.getLong("id"));
            return actor;
        });
    }

    //row mapper
    private RowMapper<Movie> movieMapper() {
        return (rs, rowNum) -> mapRowToMovie(rs);
    }
    private Movie mapRowToMovie(ResultSet rs) throws SQLException {
        // 1. Director 객체 생성
        Director director = new Director(rs.getString("director_name"));
        director.setId(rs.getLong("director_id"));
        // 2. Movie 객체 생성
        Movie movie = new Movie(
            rs.getString("name"),
            director,
            Genre.valueOf(rs.getString("genre")),
            rs.getDate("release_date").toLocalDate(),
            rs.getString("description"),
            new ArrayList<>() 
        );
        movie.setId(rs.getLong("id"));
        // 3. [변경] 평점 매핑
        movie.setRating(rs.getDouble("rating"));
        return movie;
        //여기서 actors처리 안하는 이유  ->  n+1문제... findAll할시에 전체 쿼리+배우와의 관계에 대한 n번의 쿼리가 더 나감 
    }

    //ResultSetExtractor
    private ResultSetExtractor<List<Movie>> movieResultSetExtractor() {
        return rs -> {
            Map<Long, Movie> movieMap = new LinkedHashMap<>();//중복제거&순서보장
            while (rs.next()) {
                Long movieId = rs.getLong("id");//영화매핑
                Movie movie = movieMap.computeIfAbsent(movieId, id -> {
                    try {
                        return mapRowToMovie(rs); // 우리가 만든 mapRowToMovie 재사용
                    } catch (SQLException e) {
                        throw new RuntimeException("데이터 매핑 실패", e);
                    }
                });

                Long actorId = rs.getObject("actor_id", Long.class);//배우추가
                if (actorId != null) {
                    Actor actor = new Actor(rs.getString("actor_name"));
                    actor.setId(actorId);
                    // 중복 추가 방지 (이미 들어있는 배우인지 확인)
                    if (!movie.getActors().contains(actor)) {
                        movie.addActor(actor);
                    }
                }
            }
            return new ArrayList<>(movieMap.values());
        };
    }
}
//save update delete(cascade) 시에는 actors-movies의 관계엔티티고려
//조회시에는 rowmapper로 매핑하는것 뿐만아니라 actor도 같이 처리해서 movie객체에 출연한 모든 actor들을 넣어줘야함