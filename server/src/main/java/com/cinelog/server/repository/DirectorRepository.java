package com.cinelog.server.repository;

import java.util.List;
import java.util.Optional;

import com.cinelog.server.domain.Director;

public interface DirectorRepository {
    public Director save(Director director);
    public Optional<Director> findById(Long id);
    public List<Director> findAll();
    public List<Director> findAllByNameContaining(String keyword);
    public boolean delete(Long id);
}
