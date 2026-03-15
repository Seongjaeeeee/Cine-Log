package com.cinelog.server.repository.jdbc;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDateTime;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;

@JdbcTest // JDBC 관련 빈만 로드 
@Import(UserJdbcRepository.class)// 테스트 대상 클래스를 직접 주입
class UserJdbcRepositoryTest {
    @Autowired
    private UserJdbcRepository userRepository;

    @Test
    @DisplayName("신규 유저를 저장하면 PK(id)가 생성되어야 한다")
    void saveInsertTest() {
        User user = new User("tester1", "pass123","email@1", Role.USER);
        
        User savedUser = userRepository.save(user);

        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("tester1");
        assertThat(savedUser.isDeleted()).isEqualTo(false);
        assertThat(savedUser.getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("기존 유저의 정보를 수정하면 DB에 반영되어야 한다")
    void saveUpdateTest() {
        User user = userRepository.save(new User("oldName", "pass", "email@1",Role.USER));
        Long savedId = user.getId();
        User updatedUser = new User("newName", "newPass", "email@1",Role.ADMIN,false);
        updatedUser.setId(savedId);

        userRepository.save(updatedUser);

        Optional<User> found = userRepository.findByName("newName");
        assertThat(found).isPresent();
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(found.get().getPassword()).isEqualTo("newPass");
        assertThat(found.get().isDeleted()).isEqualTo(false);
    }

    @Test
    @DisplayName("이름으로 유저를 검색할 때 정보가 정확히 매핑되어야 한다")
    void findByNameTest() {
        // Given
        userRepository.save(new User("findMe", "pass123","email@1", Role.ADMIN));

        // When
        Optional<User> found = userRepository.findByName("findMe");

        // Then
        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("findMe");
        assertThat(found.get().getRole()).isEqualTo(Role.ADMIN);
        assertThat(found.get().getPassword()).isEqualTo("pass123");
    }

    @Test
    @DisplayName("id로 유저 검색시 정보가 매핑되어야한다")
    void findByidTest(){
        User user = userRepository.save(new User("Name", "pass","email@1", Role.USER));
        Long savedId = user.getId();

        Optional<User> found = userRepository.findById(savedId);

        assertThat(found).isPresent();
        assertThat(found.get().getName()).isEqualTo("Name");
        assertThat(found.get().getRole()).isEqualTo(Role.USER);
        assertThat(found.get().getPassword()).isEqualTo("pass");
        assertThat(found.get().getCreatedAt()).isBeforeOrEqualTo(LocalDateTime.now());
    }

    @Test
    @DisplayName("유저 이름이 존재하면 true, 없으면 false를 반환해야 한다")
    void existsByNameTest() {
        // Given
        userRepository.save(new User("exists", "pass","email@1", Role.USER));

        // When & Then
        assertThat(userRepository.existsByName("exists")).isTrue();
        assertThat(userRepository.existsByName("none")).isFalse();
    }
    
    @Test
    @DisplayName("유저 이름이 존재하면 true, 없으면 false를 반환해야 한다")
    void existsByNameEmail() {
        // Given
        userRepository.save(new User("exists", "pass","email@1", Role.USER));

        // When & Thens
        assertThat(userRepository.existsByEmail("email@1")).isTrue();
        assertThat(userRepository.existsByEmail("none")).isFalse();
    }

    @Test
    @DisplayName("유저를 탈퇴 처리(deleted=true)하면 조회 로직에서 제외되어야 한다")
    void softDeleteTest() {
        // Given: 유저 가입
        User user = userRepository.save(new User("deleteMe", "pass","email@1", Role.USER));
        Long userId = user.getId();

        // When: 탈퇴 처리 (deleted = true로 업데이트)
        User withdrawingUser = new User("deleteMe", "pass","email@1", Role.USER, true); // true 설정
        withdrawingUser.setId(userId);
        userRepository.save(withdrawingUser);

        // Then 1: ID로 조회 시 없어야 함 (Empty)
        assertThat(userRepository.findById(userId)).isEmpty();

        // Then 2: 이름으로 조회 시 없어야 함 (Empty)
        assertThat(userRepository.findByName("deleteMe")).isEmpty();

        // Then 3: 존재 여부 확인 시 false여야 함
        assertThat(userRepository.existsByName("deleteMe")).isFalse();
    }
}