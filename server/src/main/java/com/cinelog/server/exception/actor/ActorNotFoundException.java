package com.cinelog.server.exception.actor;

public class ActorNotFoundException extends RuntimeException {
    public ActorNotFoundException(Long id) {
        super("배우를 찾을 수 없습니다. (ID: " + id + ")");
    }
}