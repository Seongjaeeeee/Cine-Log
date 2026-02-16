package com.cinelog.server.domain.event;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public class ReviewChangedEvent {
    private final Long movieId;
}