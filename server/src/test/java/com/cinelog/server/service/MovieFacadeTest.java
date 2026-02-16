package com.cinelog.server.service;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;
import com.cinelog.server.exception.actor.ActorNotFoundException;
import com.cinelog.server.exception.director.DirectorNotFoundException;
import com.cinelog.server.exception.movie.MovieNotFoundException;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieFacade 오케스트레이션 테스트")
class MovieFacadeTest {

    @Mock
    private MovieService movieService;
    @Mock
    private ActorService actorService;
    @Mock
    private DirectorService directorService;

    @InjectMocks
    private MovieFacade movieFacade;

    // --- 1. 영화 생성 (가장 복잡한 로직) ---

    @Test
    @DisplayName("영화 생성 성공: ID로 감독과 배우들을 조회하여 영화 생성 서비스에 전달한다")
    void createMovie_Success() {
        // Given
        Long directorId = 1L;
        Long[] actorIds = {10L, 20L}; // 가변 인자 테스트
        
        Director mockDirector = new Director("봉준호");
        Actor mockActor1 = new Actor("송강호");
        Actor mockActor2 = new Actor("이선균");

        // 각 서비스가 ID를 받으면 정해진 객체를 반환하도록 Stubbing
        given(directorService.getDirectorById(directorId)).willReturn(mockDirector);
        given(actorService.getActorById(10L)).willReturn(mockActor1);
        given(actorService.getActorById(20L)).willReturn(mockActor2);

        // When
        movieFacade.createMovie("기생충", Genre.THRILLER, LocalDate.now(), "설명", directorId, actorIds);

        // Then
        // 1. 순서대로 조회 서비스가 호출되었는지 확인
        verify(directorService).getDirectorById(directorId);
        verify(actorService, times(2)).getActorById(anyLong()); // 배우가 2명이니 2번 호출
        // 2. 조회된 객체들이 movieService.createMovie에 정확히 전달되었는지 인자들을 확인
        verify(movieService).createMovie(
                eq("기생충"), 
                eq(mockDirector), 
                eq(Genre.THRILLER), 
                any(LocalDate.class), 
                eq("설명"), 
                argThat(list -> list.contains(mockActor1) && list.contains(mockActor2)) // 리스트 내부 검증
        );
    }

    @Test
    @DisplayName("영화 생성 실패: 감독 조회에 실패하면 영화 생성 로직은 호출되지 않는다")
    void createMovie_Fail_DirectorNotFound() {
        // Given
        Long invalidDirectorId = 999L;
        given(directorService.getDirectorById(invalidDirectorId))
                .willThrow(new DirectorNotFoundException(invalidDirectorId));

        // When & Then
        assertThatThrownBy(() -> 
            movieFacade.createMovie("제목", Genre.DRAMA, LocalDate.now(), "설명", invalidDirectorId)
        ).isInstanceOf(DirectorNotFoundException.class);
        // 예외가 발생했으므로 배우 조회나 영화 생성은 절대 실행되면 안 됨 (Fail-Fast)
        verify(actorService, never()).getActorById(anyLong());
        verify(movieService, never()).createMovie(any(), any(), any(), any(), any(), any());
    }

    @Test
    @DisplayName("영화 생성 실패: 배우 조회 중 한 명이라도 없으면 중단되고 예외가 발생한다")
    void createMovie_Fail_ActorNotFound() {
        // Given
        Long directorId = 1L;
        Long existingActorId = 10L;
        Long unknownActorId = 999L; // 존재하지 않는 배우 ID
        Long[] actorIds = {existingActorId, unknownActorId};

        given(directorService.getDirectorById(directorId)).willReturn(new Director("봉준호"));
        given(actorService.getActorById(existingActorId)).willReturn(new Actor("송강호"));
        given(actorService.getActorById(unknownActorId))
                .willThrow(new ActorNotFoundException(unknownActorId));

        // When & Then
        assertThatThrownBy(() -> 
            movieFacade.createMovie("제목", Genre.DRAMA, LocalDate.now(), "설명", directorId, actorIds)
        ).isInstanceOf(ActorNotFoundException.class);
        // 검증: 영화 생성 서비스는 절대 호출되면 안 됨
        verify(movieService, never()).createMovie(any(), any(), any(), any(), any(), any());
    }

    // --- 2. 감독 교체 ---

    @Test
    @DisplayName("감독 교체 성공: 영화와 새 감독을 조회하여 업데이트 서비스에 전달한다")
    void updateMovieDirector_Success() {
        // Given
        Long movieId = 100L;
        Long newDirectorId = 200L;
        Movie mockMovie = mock(Movie.class); // Movie 객체도 Mock으로 생성 가능
        Director mockDirector = new Director("새 감독");
        given(movieService.getMovieById(movieId)).willReturn(mockMovie);
        given(directorService.getDirectorById(newDirectorId)).willReturn(mockDirector);

        // When
        movieFacade.updateMovieDirector(movieId, newDirectorId);

        // Then
        verify(movieService).updateMovieDirector(mockMovie, mockDirector);
    }

    @Test
    @DisplayName("감독 교체 실패: 영화는 존재하지만 감독이 없으면 변경 로직이 실행되지 않는다")
    void updateMovieDirector_Fail_DirectorNotFound() {
        // Given
        Long movieId = 1L;
        Long invalidDirectorId = 999L;
        given(movieService.getMovieById(movieId)).willReturn(mock(Movie.class));
        given(directorService.getDirectorById(invalidDirectorId))
                .willThrow(new DirectorNotFoundException(invalidDirectorId));

        // When & Then
        assertThatThrownBy(() -> 
            movieFacade.updateMovieDirector(movieId, invalidDirectorId)
        ).isInstanceOf(DirectorNotFoundException.class);
        verify(movieService, never()).updateMovieDirector(any(), any());
    }

  // --- 3. 배우 추가 (Add Actor) ---

    @Test
    @DisplayName("배우 추가 성공: 영화와 배우를 조회하여 추가 서비스에 전달한다")
    void addActorToMovie_Success() {
        // Given
        Long movieId = 1L;
        Long actorId = 10L;

        // Facade 테스트에서는 도메인 로직이 중요하지 않으므로 Mock 객체 사용이 효율적
        Movie mockMovie = mock(Movie.class); 
        Actor mockActor = new Actor("송강호");
        given(movieService.getMovieById(movieId)).willReturn(mockMovie);
        given(actorService.getActorById(actorId)).willReturn(mockActor);

        // When
        movieFacade.addActorToMovie(movieId, actorId);

        // Then
        verify(movieService).addActorToMovie(mockMovie, mockActor);
    }

    @Test
    @DisplayName("배우 추가 실패: 영화는 있지만 배우가 없으면 추가 로직이 실행되지 않는다")
    void addActorToMovie_Fail_ActorNotFound() {
        // Given
        Long movieId = 1L;
        Long invalidActorId = 999L;

        given(movieService.getMovieById(movieId)).willReturn(mock(Movie.class));
        given(actorService.getActorById(invalidActorId))
                .willThrow(new ActorNotFoundException(invalidActorId));

        // When & Then
        assertThatThrownBy(() -> 
            movieFacade.addActorToMovie(movieId, invalidActorId)
        ).isInstanceOf(ActorNotFoundException.class);

        verify(movieService, never()).addActorToMovie(any(), any());
    }

    // --- 4. 배우 삭제 (Remove Actor) ---

    @Test
    @DisplayName("배우 삭제 성공: 영화와 배우를 조회하여 삭제 서비스에 전달한다")
    void removeActorFromMovie_Success() {
        // Given
        Long movieId = 1L;
        Long actorId = 10L;

        Movie mockMovie = mock(Movie.class);
        Actor mockActor = new Actor("삭제될 배우");

        given(movieService.getMovieById(movieId)).willReturn(mockMovie);
        given(actorService.getActorById(actorId)).willReturn(mockActor);

        // When
        movieFacade.removeActorFromMovie(movieId, actorId);

        // Then
        verify(movieService).removeActorFromMovie(mockMovie, mockActor);
    }

    @Test
    @DisplayName("배우 삭제 실패: 영화 조회 자체가 실패하면 배우 조회도 실행되지 않는다")
    void removeActorFromMovie_Fail_MovieNotFound() {
        // Given (이번엔 시작부터 실패하는 케이스)
        Long invalidMovieId = 999L;
        Long actorId = 10L;

        given(movieService.getMovieById(invalidMovieId))
                .willThrow(new MovieNotFoundException(invalidMovieId));

        // When & Then
        assertThatThrownBy(() -> 
            movieFacade.removeActorFromMovie(invalidMovieId, actorId)
        ).isInstanceOf(MovieNotFoundException.class);

        verify(actorService, never()).getActorById(anyLong());
        verify(movieService, never()).removeActorFromMovie(any(), any());
    }
}