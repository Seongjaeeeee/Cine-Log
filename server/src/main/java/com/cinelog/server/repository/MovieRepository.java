package com.cinelog.server.repository;

import com.cinelog.server.domain.Movie;
import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Repository;

@Repository
public interface MovieRepository {
	public Movie save(Movie movie);
	public Optional<Movie> findById(Long id);
	public List<Movie> findAll();
    public List<Movie> findAllByDirectorId(Long directorId);
    public List<Movie> findAllByActorId(Long actorId);
	public List<Movie> findAllByNameContaining(String keyword);
    public List<Movie> findAllByActorNameContaining(String keyword);
    public List<Movie> findAllByDirectorNameContaining(String keyword);
	public boolean delete(Long id);
}
