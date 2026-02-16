package com.cinelog.server.dto.actor;

import lombok.Value;

@Value // Getter, toString, equals, hashCode, 모든 필드 생성자를 한 번에!
public class ActorSearchResult {
    String name;
    Long id;
}