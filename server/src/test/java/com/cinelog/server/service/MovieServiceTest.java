package com.cinelog.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;
import com.cinelog.server.dto.movie.MovieSearchResult;
import com.cinelog.server.dto.movie.MovieUpdateRequest;
import com.cinelog.server.exception.movie.MovieNotFoundException;
import com.cinelog.server.repository.MovieRepository;

@ExtendWith(MockitoExtension.class)
@DisplayName("MovieService 테스트")
class MovieServiceTest {

    @Mock
    private MovieRepository movieRepository;
    @InjectMocks
    private MovieService movieService;

    @Test
    @DisplayName("영화 조회 테스트")
    void createMovieTest_success() {
        // Given
        String name = "기생충";
        Director director = new Director("봉준호");
        Genre genre = Genre.THRILLER;
        LocalDate releaseDate = LocalDate.of(2019, 5, 30);
        String description = "행복은 나눌수록 커지잖아요?";
        List<Actor> actors = List.of(new Actor("송강호"), new Actor("이선균"));

        // When
        movieService.createMovie(name, director, genre, releaseDate, description, actors);
        
        // Then
        verify(movieRepository, times(1)).save(any(Movie.class));
    }
    //------------------------------조회로직
    @Test
    @DisplayName("ID로 영화 조회 성공: 영화가 존재하면 해당 객체를 반환한다")
    void getMovieById_Success() {
        // Given
        Long movieId = 1L;
        Movie movie = createTestMovie("기생충");
        given(movieRepository.findById(movieId)).willReturn(Optional.of(movie));

        // When
        Movie result = movieService.getMovieById(movieId);

        // Then
        assertThat(result.getName()).isEqualTo("기생충");
    }

    @Test
    @DisplayName("ID로 영화 조회 실패: 영화가 없으면 MovieNotFoundException이 발생한다")
    void getMovieById_Fail_NotFound() {
        // Given
        Long invalidId = 999L;
        given(movieRepository.findById(invalidId)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> movieService.getMovieById(invalidId))
                .isInstanceOf(MovieNotFoundException.class);
    }

    @Test
    @DisplayName("전체 영화 조회: 저장된 모든 영화 리스트를 반환한다")
    void findAllMovies_Success() {
        // Given
        given(movieRepository.findAll()).willReturn(List.of(
                createTestMovie("영화1"),
                createTestMovie("영화2")
        ));

        // When
        List<Movie> results = movieService.findAllMovies();

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactly("영화1", "영화2");
    }
    @Test
    @DisplayName("영화 제목 키워드 검색: 엔티티 리스트를 DTO 리스트로 변환하여 반환한다")
    void findAllMoviesByKeyword_Success() {
        // Given
        String keyword = "기생";
        given(movieRepository.findAllByNameContaining(keyword))
                .willReturn(List.of(createTestMovie("기생충")));

        // When
        List<MovieSearchResult> results = movieService.findAllMoviesByKeyword(keyword);

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0)).isInstanceOf(MovieSearchResult.class);
        assertThat(results.get(0).getName()).isEqualTo("기생충");
    }
    @Test
    @DisplayName("배우 이름 키워드 검색: 해당 배우가 출연한 영화를 DTO로 반환한다")
    void findAllMovieByActorKeyword_Success() {
        // Given
        String actorName = "송강호";
        given(movieRepository.findAllByActorNameContaining(actorName))
                .willReturn(List.of(createTestMovie("기생충"), createTestMovie("택시운전사")));

        // When
        List<MovieSearchResult> results = movieService.findAllMoviesByActorKeyword(actorName);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactly("기생충", "택시운전사");
    }
    @Test
    @DisplayName("감독 이름 키워드 검색: 해당 감독의 영화를 DTO로 반환한다")
    void findAllMoviesByDirectorkeyword_Success() {
        // Given
        String directorName = "봉준호";
        given(movieRepository.findAllByDirectorNameContaining(directorName))
                .willReturn(List.of(createTestMovie("기생충"), createTestMovie("설국열차")));

        // When
        List<MovieSearchResult> results = movieService.findAllMoviesByDirectorKeyword(directorName);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactly("기생충", "설국열차");
    }
    //------------------------------수정로직
    @Test
    @DisplayName("영화 정보 수정 성공: 기존 영화의 정보가 요청 데이터로 변경되고 저장된다")
    void updateMovieInfo_Success() {
        // Given
        Long movieId = 1L;
        Movie existingMovie = new Movie("기존 제목", new Director("감독"), Genre.DRAMA, LocalDate.now(), "기존 설명", List.of());
        MovieUpdateRequest request = new MovieUpdateRequest(
                "수정된 제목",
                Genre.ACTION,
                "수정된 설명",
                LocalDate.of(2026, 2, 10)
        );
        given(movieRepository.findById(movieId)).willReturn(Optional.of(existingMovie));

        // When
        movieService.updateMovieInfo(movieId, request);
        // Then
        // 1. 기존 객체의 필드가 바뀌었는지 확인 (도메인 로직 검증)
        assertThat(existingMovie.getName()).isEqualTo("수정된 제목");
        assertThat(existingMovie.getGenre()).isEqualTo(Genre.ACTION);
        assertThat(existingMovie.getDescription()).isEqualTo("수정된 설명");
        // 2. 바뀐 객체가 save 메서드로 전달되었는지 확인 (행위 검증)
        verify(movieRepository, times(1)).save(existingMovie);
    }
    @Test
    @DisplayName("영화 감독 수정 성공: 기존 영화의 감독이 변경되고 저장된다")
    void updateMovieDirector_Success() {
        // Given
        Movie existingMovie = new Movie("기존 제목", new Director("감독"), Genre.DRAMA, LocalDate.now(), "기존 설명", List.of());
        Director newDirector = new Director("새 감독");

        // When
        movieService.updateMovieDirector(existingMovie, newDirector);

        // Then
        assertThat(existingMovie.getDirector().getName()).isEqualTo("새 감독");
        verify(movieRepository, times(1)).save(existingMovie);
    }
    @Test
    @DisplayName("배우 삭제 성공 후 저장된다")
    void removeActorFromMovie_Success() {
        // Given
        Actor remainActor = new Actor("배우1");
        Actor deletedActor = new Actor("배우2");
        remainActor.setId(1L);
        deletedActor.setId(2L);
        Movie existingMovie = new Movie("기존 제목", new Director("감독"), Genre.DRAMA, LocalDate.now(), "기존 설명", 
                                List.of(remainActor,deletedActor));
        
        // When
        movieService.removeActorFromMovie(existingMovie, deletedActor);

        // Then
        assertThat(existingMovie.getActors())
            .hasSize(1)                          
            .doesNotContain(deletedActor)       
            .containsExactly(remainActor);      

        verify(movieRepository, times(1)).save(existingMovie);
    }
     @Test
    @DisplayName("배우 추가 성공 후 저장된다")
    void addActorToMovie_Success() {
        // Given
        Actor existingActor = new Actor("배우1");
        Actor addActor = new Actor("배우2");
        existingActor.setId(1L);
        addActor.setId(2L);
        Movie existingMovie = new Movie("기존 제목", new Director("감독"), Genre.DRAMA, LocalDate.now(), "기존 설명", 
                                List.of(existingActor));
        
        // When
        movieService.addActorToMovie(existingMovie, addActor);

        // Then
        assertThat(existingMovie.getActors())
            .hasSize(2)                                
            .containsExactly(existingActor,addActor);      

        verify(movieRepository, times(1)).save(existingMovie);
    }

    @Test
    @DisplayName("영화 평점 업데이트 테스트")
    void updateMovieRating_Success(){
        //Given
        Double rating = 5.0;
        Movie movie = new Movie("제목", new Director("감독"), Genre.DRAMA, LocalDate.now(), "기존 설명", List.of());
        //when
        movieService.updateMovieRating(movie,rating);
        //then
        assertThat(movie.getRating()).isEqualTo(rating);
        verify(movieRepository).save(movie);
    }
 

    @Test
    @DisplayName("영화 삭제 테스트 성공")
    void deleteMovie_Success(){
        // Given
        Long id = 1L;
        given(movieRepository.delete(id)).willReturn(true);
        // When
        movieService.deleteMovie(id);
        // Then
        verify(movieRepository, times(1)).delete(id);
    }

    @Test
    @DisplayName("영화 삭제 테스트 실패할시 예외 발생")
    void deleteMovie_Fail() {
        // Given
        Long id = 1L;
        given(movieRepository.delete(id)).willReturn(false);
    
        assertThatThrownBy(() -> movieService.deleteMovie(id))
                .isInstanceOf(MovieNotFoundException.class);
    }
    
    private Movie createTestMovie(String name) {
        return new Movie(name, new Director("감독"), Genre.DRAMA, LocalDate.now(), "설명", List.of());
    }
}
//도메인 로직에서 반환하는 오류도 여기서 테스트 해야하나??
//배우 추가 로직(addActorToMovie)에서 이미 존재하는 배우를 또 추가하려고 할 때의 동작입니다이나
//때 도메인(Movie) 내부의 validateConstructor나 수정 로직에서 던지는 예외가 서비스 계층까지 잘 전달되는지 확인 이런거