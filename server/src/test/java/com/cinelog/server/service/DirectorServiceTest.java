package com.cinelog.server.service;

import com.cinelog.server.domain.Director;
import com.cinelog.server.dto.director.DirectorSearchResult;
import com.cinelog.server.exception.director.DirectorNotFoundException;
import com.cinelog.server.repository.DirectorRepository;
import com.cinelog.server.repository.MovieRepository;
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
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DirectorServiceTest {

    @Mock
    private DirectorRepository directorRepository;
    @Mock 
    private MovieRepository movieRepository;
    @InjectMocks
    private DirectorService directorService;

    @Test
    @DisplayName("감독 생성 시 저장 메서드가 호출된다")
    void createDirector_Success() {
        // Given
        String name = "봉준호";
        // When
        directorService.createDirector(name);
        // Then
        verify(directorRepository, times(1)).save(any(Director.class));
    }


    @Test
    @DisplayName("ID로 감독 조회 시 존재하면 반환한다")
    void getDirectorById_Success() {
        // Given
        Long id = 1L;
        Director director = new Director("박찬욱");
        given(directorRepository.findById(id)).willReturn(Optional.of(director));
        // When
        Director result = directorService.getDirectorById(id);
        // Then
        assertThat(result.getName()).isEqualTo("박찬욱");
    }
    @Test
    @DisplayName("ID로 감독 조회 시 없으면 DirectorNotFoundException을 던진다")
    void getDirectorById_Fail_NotFound() {
        // Given
        Long id = 999L;
        given(directorRepository.findById(id)).willReturn(Optional.empty());
        // When & Then
        assertThatThrownBy(() -> directorService.getDirectorById(id))
                .isInstanceOf(DirectorNotFoundException.class)
                .hasMessageContaining(String.valueOf(id));
    }

    @Test
    @DisplayName("키워드로 검색 시 DTO로 변환되어 반환된다")
    void searchDirectors_Success() {
        // Given
        String keyword = "봉";
        Director d1 = new Director("봉준호");
        Director d2 = new Director("봉태규");
        given(directorRepository.findAllByNameContaining(keyword)).willReturn(List.of(d1, d2));

        // When
        List<DirectorSearchResult> results = directorService.findAllDirectorsByKeyword(keyword);

        // Then
        assertThat(results).hasSize(2);
        assertThat(results.get(0)).isInstanceOf(DirectorSearchResult.class);
        assertThat(results.get(0).getName()).isEqualTo("봉준호");
    }

    @Test
    @DisplayName("전체 요청시 전체 배열을 반환한다")
    void findAllDirectors_Success(){
        Director d1 = new Director("감독1");
        Director d2 = new Director("감독2");
        given(directorRepository.findAll()).willReturn(List.of(d1,d2));

        List<Director> results = directorService.findAllDirectors();
        
        assertThat(results).hasSize(2);
        assertThat(results.get(0).getName()).isEqualTo("감독1");
    }
    
    @Test
    @DisplayName("감독 수정 시 이름이 변경되고 저장이 호출된다")
    void updateDirector_Success() {
        // Given
        Long id = 1L;
        String newName = "변경된이름";
        Director director = new Director("기존이름");
        given(directorRepository.findById(id)).willReturn(Optional.of(director));

        // When
        directorService.updateDirector(id, newName);

        // Then
        assertThat(director.getName()).isEqualTo(newName);
        verify(directorRepository, times(1)).save(director);
    }

    // --- 아래부터는 삭제 관련 로직 (가장 중요!) ---

    @Test
    @DisplayName("감독 삭제 성공: 연출한 영화가 0편이고 존재할 때 삭제된다")
    void deleteDirector_Success() {
        // Given
        Long id = 1L;
        given(movieRepository.countByDirectorId(id)).willReturn(0);
        given(directorRepository.delete(id)).willReturn(true);

        // When
        directorService.deleteDirector(id);

        // Then
        verify(movieRepository, times(1)).countByDirectorId(id); // 검사 했는지 확인
        verify(directorRepository, times(1)).delete(id); // 삭제 시도 했는지 확인
    }

    @Test
    @DisplayName("감독 삭제 실패: 연출한 영화가 1편 이상이면 예외가 발생하고 삭제되지 않는다")
    void deleteDirector_Fail_HasMovies() {
        // Given
        Long id = 1L;
        given(movieRepository.countByDirectorId(id)).willReturn(3);

        // When & Then
        assertThatThrownBy(() -> directorService.deleteDirector(id))
                .isInstanceOf(IllegalStateException.class) // 예외 타입 확인
                .hasMessageContaining("3편 존재하여 삭제할 수 없습니다"); // 메시지 검증

        // 중요: 예외가 터졌으므로 delete() 메서드는 절대 호출되면 안 됨!
        verify(directorRepository, times(0)).delete(any());
    }

    @Test
    @DisplayName("감독 삭제 실패: 영화는 없지만 존재하지 않는 감독 ID인 경우 예외 발생")
    void deleteDirector_Fail_NotFound() {
        // Given
        Long id = 999L;
        given(movieRepository.countByDirectorId(id)).willReturn(0);
        given(directorRepository.delete(id)).willReturn(false);

        // When & Then
        assertThatThrownBy(() -> directorService.deleteDirector(id))
                .isInstanceOf(DirectorNotFoundException.class);
    }
}