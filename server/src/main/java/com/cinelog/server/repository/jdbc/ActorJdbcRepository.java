package com.cinelog.server.repository.jdbc;

import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.BeanPropertySqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.repository.ActorRepository;
@Repository
public class ActorJdbcRepository implements ActorRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public ActorJdbcRepository(DataSource dataSource){
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)//인서트문 자동생성
                .withTableName("actors")
                .usingGeneratedKeyColumns("id");
    }
    @Override
    public Actor save(Actor actor){
        if(actor.getId() == null){
            return insert(actor);
        }
        else return update(actor);
    }
    @Override
    public Optional<Actor> findById(Long id){
        String sql = "SELECT id,name FROM actors WHERE id = :id";
        try{
            Actor actor = jdbcTemplate.queryForObject(sql, Map.of("id", id), actorMapper);
            return Optional.ofNullable(actor);
        }
        catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    @Override
    public List<Actor> findAll(){
        String sql = "SELECT id, name FROM actors";
        return jdbcTemplate.query(sql, actorMapper);
    }
    @Override
    public List<Actor> findAllByNameContaining(String keyword){
        String sql = "SELECT id, name FROM actors WHERE name LIKE :keyword";
        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql,Map.of("keyword",likeKeyword),actorMapper);
    }
    @Override
    public boolean delete(Long id){//관계테이블 삭제는 cascade로 설정
        String sql = "DELETE FROM actors WHERE id = :id";
        int affectedRows = jdbcTemplate.update(sql,Map.of("id",id));
        return affectedRows>0;
    }

    private Actor insert(Actor actor){
        SqlParameterSource params = new BeanPropertySqlParameterSource(actor);
        Number key = jdbcInsert.executeAndReturnKey(params);
        actor.setId(key.longValue());
        return actor;
    }
    private Actor update(Actor actor){
        String sql = "UPDATE actors SET name = :name WHERE id = :id";
        SqlParameterSource params = new BeanPropertySqlParameterSource(actor);
        int affectedRows =  jdbcTemplate.update(sql,params);
        if(affectedRows == 0)throw new RuntimeException("해당 ID의 배우를 찾을 수 없습니다: " + actor.getId());
        return actor;
    }

    private final RowMapper<Actor> actorMapper = (rs, rowNum) -> {
        Actor actor = new Actor(rs.getString("name"));
        actor.setId(rs.getLong("id"));
        return actor;
    };
}