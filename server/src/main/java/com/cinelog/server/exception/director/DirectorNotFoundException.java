package com.cinelog.server.exception.director;

public class DirectorNotFoundException extends RuntimeException{
     public DirectorNotFoundException(Long id) {
        super("감독을 찾을 수 없습니다. (ID: " + id + ")");
    }
}
