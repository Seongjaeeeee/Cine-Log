package com.cinelog.server.dto.movie;

import java.time.LocalDate;

import com.cinelog.server.domain.Genre;

import lombok.Value;

@Value
public class MovieUpdateRequest {
    private String name;
    private Genre genre;
    private String description;
    private LocalDate releaseDate;
}