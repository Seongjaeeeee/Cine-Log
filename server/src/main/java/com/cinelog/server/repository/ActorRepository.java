package com.cinelog.server.repository;

import com.cinelog.server.domain.Actor;
import java.util.List;
import java.util.Optional;

public interface ActorRepository {
    public Actor save(Actor actor);
    public Optional<Actor> findById(Long id);
    public List<Actor> findAll();
    public List<Actor> findAllByNameContaining(String keyword);
    public boolean delete(Long id);
}