package com.cinelog.server.exception.movie;

public class MovieNotFoundException extends RuntimeException{
    public MovieNotFoundException(Long id) {
        super("영화를 찾을 수 없습니다. (ID: " + id + ")");
    }
}
