package com.cinelog.server.service;

import com.cinelog.server.domain.Actor;
import com.cinelog.server.dto.actor.ActorSearchResult;
import com.cinelog.server.exception.actor.ActorNotFoundException;
import com.cinelog.server.repository.ActorRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class) // Mockito 환경 활성화
class ActorServiceTest {

    @Mock
    private ActorRepository actorRepository;
    @InjectMocks 
    private ActorService actorService;

    @Test
    @DisplayName("배우 생성 시 저장 메서드가 호출되고 생성된 객체가 반환된다")
    void createActor_Success() {
        // Given
        String name = "송강호";
        // When
        Actor result = actorService.createActor(name);
        // Then
        assertThat(result.getName()).isEqualTo(name);
        verify(actorRepository, times(1)).save(any(Actor.class));
    }
    @Test
    @DisplayName("이름이 없으면 도메인 검증에 의해 예외가 발생하고 저장은 호출되지 않는다")
    void createActor_Fail_Validation() {
        // Given
        String emptyName = "";
        // When & Then
        assertThatThrownBy(() -> actorService.createActor(emptyName))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("이름은 필수입니다.");
        // 예외가 터졌으니 저장 로직은 실행되면 안 됨
        verify(actorRepository, times(0)).save(any());
    }

    @Test
    @DisplayName("ID로 배우 조회 시 존재하면 반환한다")
    void getActorById_Success() {
        // Given
        Long id = 1L;
        Actor actor = new Actor("황정민");
        given(actorRepository.findById(id)).willReturn(Optional.of(actor));
        // When
        Actor result = actorService.getActorById(id);
        // Then
        assertThat(result.getName()).isEqualTo("황정민");
    }
    @Test
    @DisplayName("ID로 배우 조회 시 없으면 ActorNotFoundException을 던진다")
    void getActorById_Fail_NotFound() {
        // Given
        Long id = 999L;
        given(actorRepository.findById(id)).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> actorService.getActorById(id))
                .isInstanceOf(ActorNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }
    
    @Test
    @DisplayName("요청시 전체 배우를 반환한다")
    void findAllActors_Success() {
        Actor actor = new Actor("황정민");
        Actor actor1 = new Actor("이성민");
        Actor actor2 = new Actor("유재석");
        given(actorRepository.findAll()).willReturn(List.of(actor, actor1,actor2));

        // When
        List<Actor> results = actorService.findAllActors();

        // Then
        assertThat(results).hasSize(3);
        assertThat(results.get(0).getName()).isEqualTo("황정민");
    }

    @Test
    @DisplayName("키워드로 검색 시 DTO로 변환되어 반환된다")
    void searchActors_Success() {
        // Given
        String keyword = "송";
        Actor actor1 = new Actor("송강호");
        actor1.setId(1L);
        Actor actor2 = new Actor("송중기");
        actor2.setId(2L);

        given(actorRepository.findAllByNameContaining(keyword))
                .willReturn(List.of(actor1, actor2));

        // When
        List<ActorSearchResult> results = actorService.findAllActorsByKeyword(keyword);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isInstanceOf(ActorSearchResult.class); // DTO 변환 확인
        assertThat(results.get(0).getName()).isEqualTo("송강호");
    }

    @Test
    @DisplayName("배우 수정 시 이름이 변경되고 저장이 호출된다")
    void updateActor_Success() {
        // Given
        Long id = 1L;
        String newName = "변경된이름";
        Actor actor = new Actor("기존이름");
        given(actorRepository.findById(id)).willReturn(Optional.of(actor));

        // When
        actorService.updateActor(id, newName);

        // Then
        assertThat(actor.getName()).isEqualTo(newName);
        verify(actorRepository, times(1)).save(actor);
    }

    @Test
    @DisplayName("배우 삭제 성공 시 리포지토리의 delete가 호출된다")
    void deleteActor_Success() {
        // Given
        Long id = 1L;
        given(actorRepository.delete(id)).willReturn(true); // 삭제 성공 가정

        // When
        actorService.deleteActor(id);

        // Then
        verify(actorRepository, times(1)).delete(id);
    }

    @Test
    @DisplayName("존재하지 않는 배우 삭제 시 예외가 발생한다")
    void deleteActor_Fail_NotFound() {
        // Given
        Long id = 999L;
        given(actorRepository.delete(id)).willReturn(false); // 삭제 실패(대상 없음) 가정

        // When & Then
        assertThatThrownBy(() -> actorService.deleteActor(id))
                .isInstanceOf(ActorNotFoundException.class);
    }
}