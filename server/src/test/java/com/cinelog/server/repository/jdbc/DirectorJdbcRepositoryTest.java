package com.cinelog.server.repository.jdbc;

import com.cinelog.server.domain.Director;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;

@JdbcTest
@Import(DirectorJdbcRepository.class)
class DirectorJdbcRepositoryTest {

    @Autowired
    private DirectorJdbcRepository directorRepository;

    @Test
    @DisplayName("새로운 감독을 저장하면 고유 ID가 발급되어야 한다")
    void saveInsertTest() {
        // Given
        Director director = new Director("봉준호");

        // When
        Director saved = directorRepository.save(director);

        // Then
        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getName()).isEqualTo("봉준호");
    }

    @Test
    @DisplayName("감독 이름을 수정하면 DB에 성공적으로 반영되어야 한다")
    void saveUpdateTest() {
        // Given
        Director saved = directorRepository.save(new Director("이름 오타"));
        saved.changeName("제임스 카메론"); // 도메인 메서드 사용

        // When
        directorRepository.save(saved); // ID가 있으므로 update 호출

        // Then
        Director updated = directorRepository.findById(saved.getId()).get();//영속성을 확인해야하기 때문에 직접 db에서 다시 조회해서 확인
        assertThat(updated.getName()).isEqualTo("제임스 카메론");
    }

    @Test
    @DisplayName("감독 ID로 정보를 정확하게 조회할 수 있어야 한다")
    void findByIdTest() {
        // Given
        Director saved = directorRepository.save(new Director("박찬욱"));

        // When
        Optional<Director> found = directorRepository.findById(saved.getId());

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("박찬욱");
    }

    @Test
    @DisplayName("이름 키워드 검색 시 해당되는 모든 감독을 반환해야 한다")
    void findAllByNameContainingTest() {
        // Given
        directorRepository.save(new Director("스티븐 스필버그"));
        directorRepository.save(new Director("스티븐 소더버그"));
        directorRepository.save(new Director("크리스토퍼 놀란"));

        // When
        List<Director> results = directorRepository.findAllByNameContaining("스티븐");

        // Then
        assertThat(results).hasSize(2);
        assertThat(results).extracting("name").containsExactlyInAnyOrder("스티븐 스필버그", "스티븐 소더버그");
    }

    @Test
    @DisplayName("감독을 삭제하면 더 이상 조회되지 않아야 한다")
    void deleteTest() {
        // Given
        Director saved = directorRepository.save(new Director("삭제될 감독"));
        Long id = saved.getId();

        // When
        boolean deleted = directorRepository.delete(id);

        // Then
        assertThat(deleted).isTrue();
        assertThat(directorRepository.findById(id)).isEmpty();
    }
}