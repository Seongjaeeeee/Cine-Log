package com.cinelog.server.repository;

import java.util.List;
import java.util.Optional;

import com.cinelog.server.domain.Movie;

public interface MovieRepository {
	public Movie save(Movie movie);
	public Optional<Movie> findById(Long id);
	public List<Movie> findAll();
    public List<Movie> findAllByDirectorId(Long directorId);
    public List<Movie> findAllByActorId(Long actorId);
	public List<Movie> findAllByNameContaining(String keyword);
    public List<Movie> findAllByActorNameContaining(String keyword);
    public List<Movie> findAllByDirectorNameContaining(String keyword);
	public Integer countByDirectorId(Long id);
	public boolean delete(Long id);
}
