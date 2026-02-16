package com.cinelog.server.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
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
@Import({ActorJdbcRepository.class, DirectorJdbcRepository.class, MovieJdbcRepository.class})
class ActorJdbcRepositoryTest {

    @Autowired
    private ActorJdbcRepository actorRepository;
    @Autowired
    private DirectorJdbcRepository directorRepository;
    @Autowired
    private MovieJdbcRepository movieRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    @DisplayName("배우 정보를 저장하면 자동 생성된 ID가 포함되어야 한다")
    void saveInsertTest() {
        // Given
        Actor actor = new Actor("송강호");

        // When
        Actor savedActor = actorRepository.save(actor);

        // Then
        assertThat(savedActor.getId()).isNotNull();
        assertThat(savedActor.getName()).isEqualTo("송강호");
    }

    @Test
    @DisplayName("배우 정보를 수정하면 DB에 반영되어야 한다")
    void saveUpdateTest() {
        // Given
        Actor actor = actorRepository.save(new Actor("조우진"));
        Long id = actor.getId();
        
        Actor updatedActor = new Actor("진선규");
        updatedActor.setId(id);

        // When
        actorRepository.save(updatedActor);

        // Then
        Actor found = actorRepository.findById(id).get();
        assertThat(found.getName()).isEqualTo("진선규");
    }

    @Test
    @DisplayName("ID로 배우를 정확하게 조회할 수 있어야 한다")
    void findByIdTest() {
        // Given
        Actor saved = actorRepository.save(new Actor("박해일"));

        // When
        Optional<Actor> found = actorRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("박해일");
    }

    @Test
    @DisplayName("이름에 키워드가 포함된 모든 배우를 조회해야 한다")
    void findAllByNameContainingTest() {
        // Given
        actorRepository.save(new Actor("김윤석"));
        actorRepository.save(new Actor("김혜수"));
        actorRepository.save(new Actor("이정재"));

        // When
        List<Actor> results = actorRepository.findAllByNameContaining("김");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("김윤석", "김혜수");
    }

    @Test
    @DisplayName("배우를 삭제하면 조회되지 않아야 하며 배우-영화 관계 테이블도 삭제되어야한다")
    void deleteTest() {
        Director d = directorRepository.save(new Director("감독"));
        Actor a = actorRepository.save(new Actor("삭제될 배우"));
        movieRepository.save(new Movie("출연 영화", d, Genre.DRAMA, LocalDate.now(), "", List.of(a)));
        Long id = a.getId();

        // When
        boolean result = actorRepository.delete(id);

        // Then
        assertThat(result).isTrue();
        assertThat(actorRepository.findById(id)).isEmpty();

        Integer relationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM movie_actor WHERE actor_id = ?", Integer.class, id);
        assertThat(relationCount).isEqualTo(0);
    }
}