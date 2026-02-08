package com.cinelog.server.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.domain.Director;
import com.cinelog.server.domain.Genre;
import com.cinelog.server.domain.Movie;

@JdbcTest
@Import({MovieJdbcRepository.class, DirectorjdbcRepository.class, ActorJdbcRepository.class})// 테스트 셋업을 위해 배우/감독 레포지토리도 함께사용
class MovieJdbcRepositoryTest {

    @Autowired
    private MovieJdbcRepository movieRepository;
    @Autowired
    private DirectorjdbcRepository directorRepository;
    @Autowired
    private ActorJdbcRepository actorRepository;
        
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("영화 저장 시 영화정보, 감독과 배우 관계까지 DB에 실제로 기록되어야 한다")
    void saveInsertTest() {
        //Given
        Director director = directorRepository.save(new Director("봉준호"));
        Actor actor1 = actorRepository.save(new Actor("송강호"));
        Actor actor2 = actorRepository.save(new Actor("최우식"));

        List<Actor> actors = new ArrayList<>();
        actors.add(actor1);
        actors.add(actor2);

        Movie movie = new Movie("기생충", director, Genre.DRAMA, LocalDate.of(2019, 5, 30), "천만 영화", actors);

        // When
        Movie savedMovie = movieRepository.save(movie);
        Long savedId = savedMovie.getId(); 

        // Then
        Movie foundMovie = movieRepository.findById(savedId)//영화 저장확인
                .orElseThrow(() -> new AssertionError("DB에 영화가 저장되지 않았습니다."));
        assertThat(foundMovie.getName()).isEqualTo("기생충");
        assertThat(foundMovie.getDirector().getName()).isEqualTo("봉준호");//감독 저장 확인
        
        assertThat(foundMovie.getActors()).hasSize(2)//배우 저장 확인
                .extracting("name")
                .containsExactlyInAnyOrder("송강호", "최우식");

        assertThat(foundMovie.getRatingPolicyType()).isEqualTo("BASIC");//정책 저장 확인
    }

    @Test
    @DisplayName("영화 정보를 수정하고 배우 목록을 변경하면 DB에 반영되어야 한다")
    void saveUpdateTest() {
        // Given
        Director d = directorRepository.save(new Director("류승완"));
        Actor a1 = actorRepository.save(new Actor("황정민"));
        Actor a2 = actorRepository.save(new Actor("유아인"));
        
        List<Actor> actors = new ArrayList<>();
        actors.add(a1); // 처음엔 황정민만 출연

        Movie movie = new Movie("베테랑", d, Genre.ACTION, LocalDate.now(), "천만", actors);
        Movie saved = movieRepository.save(movie);

        List<Actor> newActors = new ArrayList<>();
        newActors.add(a2); // 유아인으로 변경

        Movie updateTarget = new Movie("베테랑2", d, Genre.ACTION, LocalDate.now(), "속편", newActors);
        updateTarget.setId(saved.getId());
        
        //when
        movieRepository.save(updateTarget); // update 실행

        // Then: DB에서 다시 조회해서 확인
        Movie updated = movieRepository.findById(saved.getId()).get();
        
        assertThat(updated.getName()).isEqualTo("베테랑2");
        assertThat(updated.getActors()).hasSize(1);
        assertThat(updated.getActors().get(0).getName()).isEqualTo("유아인"); // 황정민은 사라지고 유아인이 있어야 함
    }

    @Test
    @DisplayName("ID로 조회 시 영화 정보와 연관된 배우 목록을 가져와야 한다")
    void findByIdTest() {
        // Given
        Director director = directorRepository.save(new Director("크리스토퍼 놀란"));
        Actor actor = actorRepository.save(new Actor("킬리언 머피"));
        
        List<Actor> actors = new ArrayList<>();
        actors.add(actor);

        Movie movie = new Movie("오펜하이머", director, Genre.DRAMA, LocalDate.of(2023, 8, 15), "핵개발", actors);
        Movie saved = movieRepository.save(movie);

        // When
        Optional<Movie> found = movieRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("오펜하이머");
        assertThat(found.get().getDirector().getId()).isEqualTo(director.getId());
        assertThat(found.get().getActors()).hasSize(1);
        assertThat(found.get().getActors().get(0).getName()).isEqualTo("킬리언 머피");
        assertThat(found.get().getGenre()).isEqualTo(Genre.DRAMA);
        assertThat(found.get().getRatingPolicyType()).isEqualTo("BASIC");
    }

    @Test
    @DisplayName("findAll 조회 시 N+1 문제 없이 모든 영화와 배우를 조립해서 반환해야함")
    void findAllTest() {
        // Given
        Director d1 = directorRepository.save(new Director("감독1"));
        Actor a1 = actorRepository.save(new Actor("배우1"));
        // 영화 1: 배우 있음
        List<Actor> actors1 = new ArrayList<>();
        actors1.add(a1);
        Movie m1 = new Movie("영화1", d1, Genre.ACTION, LocalDate.now(), "설명", actors1);
        movieRepository.save(m1);
        // 영화 2: 배우 없음 (LEFT JOIN 테스트)
        Movie m2 = new Movie("영화2", d1, Genre.COMEDY, LocalDate.now(), "설명", new ArrayList<>());
        movieRepository.save(m2);

        // When
        List<Movie> movies = movieRepository.findAll();

        // Then
        assertThat(movies).hasSize(2);
        // 영화1 확인
        Movie findM1 = movies.stream().filter(m -> m.getName().equals("영화1")).findFirst().get();
        assertThat(findM1.getActors()).hasSize(1);
        // 영화2 확인
        Movie findM2 = movies.stream().filter(m -> m.getName().equals("영화2")).findFirst().get();
        assertThat(findM2.getActors()).isEmpty();
    }

    @Test
    @DisplayName("배우 ID로 검색 시 해당 배우가 출연한 영화와 '동료 배우'까지 모두 조회되어야 한다")
    void findAllByActorIdTest() {
        // Given
        Director d = directorRepository.save(new Director("최동훈"));
        Actor a1 = actorRepository.save(new Actor("김윤석")); // 타겟 배우
        Actor a2 = actorRepository.save(new Actor("김혜수")); // 동료 배우

        List<Actor> actors = new ArrayList<>();
        actors.add(a1);
        actors.add(a2);

        Movie m = new Movie("타짜", d, Genre.DRAMA, LocalDate.now(), "도박", actors);
        movieRepository.save(m);

        // When
        List<Movie> result = movieRepository.findAllByActorId(a1.getId());

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getName()).isEqualTo("타짜");
      
        assertThat(result.get(0).getActors()).hasSize(2)
                .extracting("name")
                .contains("김윤석", "김혜수");
    }

    @Test
    @DisplayName("특정 감독의 ID로 해당 감독의 모든 영화를 조회해야 한다")
    void findAllByDirectorIdTest() {
        // Given
        Director d1 = directorRepository.save(new Director("봉준호"));
        Director d2 = directorRepository.save(new Director("박찬욱"));
        
        movieRepository.save(new Movie("기생충", d1, Genre.DRAMA, LocalDate.now(), "", new ArrayList<>()));
        movieRepository.save(new Movie("마더", d1, Genre.THRILLER, LocalDate.now(), "", new ArrayList<>()));
        movieRepository.save(new Movie("올드보이", d2, Genre.ACTION, LocalDate.now(), "", new ArrayList<>()));

        // When
        List<Movie> results = movieRepository.findAllByDirectorId(d1.getId());

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("기생충", "마더");
    }

    @Test
    @DisplayName("영화 제목 키워드로 검색이 가능해야 한다")
    void findAllByNameContainingTest() {
        // Given
        Director d = directorRepository.save(new Director("감독"));
        movieRepository.save(new Movie("어벤져스: 에이지 오브 울트론", d, Genre.ACTION, LocalDate.now(), "", new ArrayList<>()));
        movieRepository.save(new Movie("울트라맨", d, Genre.ACTION, LocalDate.now(), "", new ArrayList<>()));
        movieRepository.save(new Movie("아이언맨", d, Genre.ACTION, LocalDate.now(), "", new ArrayList<>()));

        // When
        List<Movie> results = movieRepository.findAllByNameContaining("울트라");

        // Then
        assertThat(results).hasSize(1);
        assertThat(results.get(0).getName()).isEqualTo("울트라맨");
    }

    @Test
    @DisplayName("감독 이름 키워드로 해당 감독들의 영화를 모두 조회해야 한다")
    void findAllByDirectorNameContainingTest() {
        // Given
        Director d1 = directorRepository.save(new Director("김지운"));
        Director d2 = directorRepository.save(new Director("김용화"));
        
        movieRepository.save(new Movie("장화, 홍련", d1, Genre.HORROR, LocalDate.now(), "", new ArrayList<>()));
        movieRepository.save(new Movie("신과함께", d2, Genre.FANTASY, LocalDate.now(), "", new ArrayList<>()));

        // When
        List<Movie> results = movieRepository.findAllByDirectorNameContaining("김");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("장화, 홍련", "신과함께");
    }

    @Test
    @DisplayName("배우 이름 키워드로 출연 영화를 조회할 때, 영화에 출연한 모든 배우 목록이 포함되어야 한다")
    void findAllByActorNameContainingTest() {
        // Given
        Director d = directorRepository.save(new Director("최동훈"));
        Actor a1 = actorRepository.save(new Actor("조승우"));
        Actor a2 = actorRepository.save(new Actor("김윤석"));
        
        movieRepository.save(new Movie("타짜", d, Genre.DRAMA, LocalDate.now(), "", List.of(a1, a2)));

        // When
        List<Movie> results = movieRepository.findAllByActorNameContaining("조승");

        // Then
        assertThat(results).hasSize(1);
        Movie found = results.get(0);
        assertThat(found.getName()).isEqualTo("타짜");
        // '조승우'로 검색했어도 함께 출연한 '김윤석'까지 리스트에 있어야 함 (ResultSetExtractor 검증)
        assertThat(found.getActors()).extracting("name").contains("조승우", "김윤석");
    }
    
    @Test
    @DisplayName("영화를 삭제하면 연관 테이블(movie_actor) 데이터도 함께 삭제되어야 한다")
    void deleteTest() {
        // Given
        Director d = directorRepository.save(new Director("감독"));
        Actor a = actorRepository.save(new Actor("배우"));
        List<Actor> actors = new ArrayList<>();
        actors.add(a);
        
        Movie m = new Movie("삭제될 영화", d, Genre.HORROR, LocalDate.now(), "", actors);
        Movie saved = movieRepository.save(m);

        // When
        boolean isDeleted = movieRepository.delete(saved.getId());

        // Then
        assertThat(isDeleted).isTrue();
        assertThat(movieRepository.findById(saved.getId())).isEmpty();
        // 레포지토리 메서드를 쓰지 않고 JdbcTemplate으로 직접 쿼리를 날립니다.
        Integer relationCount = jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM movie_actor WHERE movie_id = ?",Integer.class,saved.getId());
        assertThat(relationCount).isEqualTo(0);
    }
}