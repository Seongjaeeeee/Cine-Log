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

import com.cinelog.server.domain.Director;
import com.cinelog.server.repository.DirectorRepository;
@Repository
public class DirectorjdbcRepository implements DirectorRepository{
    private final NamedParameterJdbcTemplate jdbcTemplate;//-> update는 cud에사용 query는 r에 사용
    private final SimpleJdbcInsert jdbcInsert;
    public DirectorjdbcRepository(DataSource dataSource) {
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);//이름으로 sql문과 매핑하도록 해줌
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)//인서트문 자동생성
                .withTableName("directors")
                .usingGeneratedKeyColumns("id");
    }
    @Override
    public Director save(Director director) {
        if (director.getId() == null) {
            return insert(director);
        }
        return update(director);
    }
    @Override
    public Optional<Director> findById(Long id){
        String sql = "SELECT id, name FROM directors where id = :id";
        try {
            Director director = jdbcTemplate.queryForObject(sql, Map.of("id", id), directorMapper);//예외발생시킴
            return Optional.ofNullable(director);//null이면 비어있는 optional 반환->사실상 의미는 x
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }
    @Override
    public List<Director> findAll(){
        String sql = "SELECT id, name FROM directors";
        return jdbcTemplate.query(sql, directorMapper);
    }
    @Override
    public List<Director> findAllByNameContaining(String keyword){
        String sql = "SELECT id, name FROM directors WHERE name LIKE :keyword";
        String likeKeyword = "%" + keyword + "%";
        return jdbcTemplate.query(sql,Map.of("keyword",likeKeyword),directorMapper);
    }
    @Override 
    public boolean delete(Long id){
        String sql = "DELETE FROM directors WHERE id = :id";
        int affectedRows = jdbcTemplate.update(sql, Map.of("id", id));// update 메서드는 영향받은 행(row)의 수를 반환합니다.
        return affectedRows > 0;
    }

    private Director insert(Director director) {
        SqlParameterSource params = new BeanPropertySqlParameterSource(director); // 객체의 필드명과 DB 컬럼명을 자동으로 매핑해서 파라미터 생성
        Number key = jdbcInsert.executeAndReturnKey(params);
        director.setId(key.longValue());
        return director;
    }
    private Director update(Director director){
        String sql = "UPDATE directors SET name = :name WHERE id = :id";
        SqlParameterSource params = new BeanPropertySqlParameterSource(director);
        int affectedRows = jdbcTemplate.update(sql, params);
        if(affectedRows == 0)throw new RuntimeException("해당 ID의 감독을 찾을 수 없습니다: " + director.getId());
        return director;
    }

    private final RowMapper<Director> directorMapper = (rs, rowNum) -> {
    Director director = new Director(rs.getString("name"));
    director.setId(rs.getLong("id"));
    return director;
    };
}