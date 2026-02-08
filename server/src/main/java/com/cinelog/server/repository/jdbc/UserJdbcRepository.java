package com.cinelog.server.repository.jdbc;

import java.util.Map;
import java.util.Optional;

import javax.sql.DataSource;

import org.springframework.dao.EmptyResultDataAccessException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.jdbc.core.namedparam.SqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcInsert;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;
import com.cinelog.server.repository.UserRepository;

@Repository
public class UserJdbcRepository implements UserRepository {
    private final NamedParameterJdbcTemplate jdbcTemplate;
    private final SimpleJdbcInsert jdbcInsert;

    public UserJdbcRepository(DataSource dataSource){
        this.jdbcTemplate = new NamedParameterJdbcTemplate(dataSource);
        this.jdbcInsert = new SimpleJdbcInsert(dataSource)
                            .withTableName("users")
                            .usingGeneratedKeyColumns("id");
    }

    @Override
    @Transactional
    public User save(User user){
        if(user.getId() == null){
            return insert(user);
        }
        return update(user);
    }
    @Override
    public boolean existsByName(String name) {
        String sql = "SELECT COUNT(*) FROM users WHERE user_name = :userName";
        Integer count = jdbcTemplate.queryForObject(sql, Map.of("userName", name), Integer.class);
        return count != null && count > 0;
    }
    @Override
    public Optional<User> findByName(String name){
        String sql = "SELECT * FROM users WHERE user_name = :userName";
        try {
            User user = jdbcTemplate.queryForObject(sql, Map.of("userName", name), userRowMapper());
            return Optional.ofNullable(user);
        } catch (EmptyResultDataAccessException e) {
            return Optional.empty();
        }
    }

    private User insert(User user){
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("user_name", user.getName())
                .addValue("password", user.getPassword())
                .addValue("role", user.getRole().name());

        Number key = jdbcInsert.executeAndReturnKey(params);
        user.setId(key.longValue());
        return user;
    }
    private User update(User user){
        String sql = "UPDATE users SET user_name = :userName, password = :password, role = :role WHERE id = :id";
        
        SqlParameterSource params = new MapSqlParameterSource()
                .addValue("id", user.getId())
                .addValue("userName", user.getName())
                .addValue("password", user.getPassword())
                .addValue("role", user.getRole().name());

        int affectedRows = jdbcTemplate.update(sql, params);
        if (affectedRows == 0) {
            throw new RuntimeException("해당 ID를 가진 유저를 찾을 수 없습니다. ID: " + user.getId());
        }
        return user;
    }

    private RowMapper<User> userRowMapper() {
        return (rs, rowNum) -> {
            User user = new User(
                rs.getString("user_name"),
                rs.getString("password"),
                Role.valueOf(rs.getString("role"))
            );
            user.setId(rs.getLong("id"));
            return user;
        };
    }
}