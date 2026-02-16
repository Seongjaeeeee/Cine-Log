package com.cinelog.server.repository;

import java.util.Optional;

import com.cinelog.server.domain.User;

public interface UserRepository {
    public User save(User user);
    public boolean existsByName(String name);
    public Optional<User> findByName(String name);
    public Optional<User> findById(Long id);
}