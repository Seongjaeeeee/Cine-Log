package com.cinelog.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.cinelog.server.dto.actor.ActorSearchResult;
import com.cinelog.server.dto.director.DirectorSearchResult;
import com.cinelog.server.dto.movie.MovieSearchResult;
import com.cinelog.server.dto.movie.PersonSearchResult;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchServiceTest")
public class SearchServiceTest {
    @Mock
    MovieService movieService;
    @Mock
    ActorService actorService;
    @Mock
    DirectorService directorService;
    @InjectMocks
    SearchService searchService;

    @Test
    @DisplayName("받은 검색결과 중 중복되는걸 제거 후 영화를 반환함")
    public void searchAllMovie_Success(){
        String keyword = "키워드";
        given(movieService.findAllMoviesByKeyword(keyword))
        .willReturn(List.of(new MovieSearchResult("영화1",1L)));
        given(movieService.findAllMoviesByActorKeyword(keyword))
        .willReturn(List.of(new MovieSearchResult("영화2",2L)));
        given(movieService.findAllMoviesByDirectorKeyword(keyword))
        .willReturn(List.of(new MovieSearchResult("영화2",2L)));

        List<MovieSearchResult> results = searchService.searchAllMovie(keyword);
        verify(movieService, times(1)).findAllMoviesByKeyword(keyword);
        verify(movieService, times(1)).findAllMoviesByActorKeyword(keyword);
        verify(movieService, times(1)).findAllMoviesByDirectorKeyword(keyword);

        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactly("영화1", "영화2");
    }

    @Test
    @DisplayName("엣지 케이스: 검색 결과가 하나도 없을 때 빈 리스트를 반환한다 (Null 아님)")
    public void searchAllMovie_Empty() {
        String keyword = "키워드";
        given(movieService.findAllMoviesByKeyword(keyword))
         .willReturn(List.of());
        given(movieService.findAllMoviesByActorKeyword(keyword))
        .willReturn(List.of());
        given(movieService.findAllMoviesByDirectorKeyword(keyword))
        .willReturn(List.of());

        List<MovieSearchResult> results = searchService.searchAllMovie(keyword);

        assertThat(results).isNotNull().isEmpty();
    }

    @Test
    @DisplayName("검색 시 배우와 감독의 인물정보가 각각 제대로 저장되어있는지 확인")
    public void searchPerson_Success(){
        String keyword = "키워드";
        ActorSearchResult actor = new ActorSearchResult("배우1",1L);
        DirectorSearchResult director = new DirectorSearchResult("감독1",1L);
        given(actorService.findAllActorsByKeyword(keyword))
        .willReturn(List.of(actor));
        given(directorService.findAllDirectorsByKeyword(keyword))
        .willReturn(List.of(director));
       

        PersonSearchResult results = searchService.searchPerson(keyword);
        verify(actorService, times(1)).findAllActorsByKeyword(keyword);
        verify(directorService, times(1)).findAllDirectorsByKeyword(keyword);
       

        assertThat(results.getActors()).containsExactly(actor);
        assertThat(results.getDirectors()).containsExactly(director);    
    }

    @Test
    @DisplayName("검색 결과 없음: 해당하는 인물이 없을 경우 빈 리스트를 포함한 DTO를 반환한다")
    void searchPerson_Empty() {
        String keyword = "없는사람";
        given(actorService.findAllActorsByKeyword(keyword)).willReturn(List.of());
        given(directorService.findAllDirectorsByKeyword(keyword)).willReturn(List.of());
        // When
        PersonSearchResult results = searchService.searchPerson(keyword);

        assertThat(results).isNotNull();
        assertThat(results.getActors())
                .isNotNull()
                .isEmpty(); 
                
        assertThat(results.getDirectors())
                .isNotNull()
                .isEmpty();
    }
}
