package com.cinelog.server.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.Optional;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;
import com.cinelog.server.exception.EntityNotFoundException;
import com.cinelog.server.exception.security.InvalidPasswordException;
import com.cinelog.server.exception.user.DuplicateException;
import com.cinelog.server.repository.UserRepository;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;
    @Mock
    private PasswordEncoder passwordEncoder;
    @InjectMocks
    private UserService userService;
    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("tester", "encodedPw","email@email.com", Role.USER);
        testUser.setId(1L); // ID 세팅
    }

    @Test
    @DisplayName("회원가입 성공: 비밀번호를 암호화하고 리포지토리에 저장한다")
    void createUser_Success() {
        // Given
        String rawPassword = "password123";
        String encodedPassword = "encodedPassword123";

        given(userRepository.existsByName("new_user")).willReturn(false); // 중복 아이디 없음
        given(userRepository.existsByEmail("email@email.com")).willReturn(false); // 중복 이메일 없음
        given(passwordEncoder.encode(rawPassword)).willReturn(encodedPassword); // 암호화 동작 정의

        // When
        User result = userService.createUser("new_user", rawPassword, "email@email.com");

        // Then
        assertThat(result.getRole()).isEqualTo(Role.USER);
        assertThat(result.getPassword()).isEqualTo("encodedPassword123");
        assertThat(result.getName()).isEqualTo("new_user");
        assertThat(result.getEmail()).isEqualTo("email@email.com");
        verify(userRepository).save(any(User.class));
    }
    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 아이디라면 예외가 발생한다")
    void createUser_Fail_DuplicateName() {
        // Given
        given(userRepository.existsByName("duplicate_user")).willReturn(true); // 이미 있음

        // When & Then
        assertThatThrownBy(() -> userService.createUser("duplicate_user", "pw","email@email.com"))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("회원가입 실패: 이미 존재하는 이메일이라면 예외가 발생한다")
    void createUser_Fail_DuplicateEmail() {
        // Given
        // 이름은 중복 아님
        given(userRepository.existsByName("new_user")).willReturn(false); 
        // 이메일은 중복임
        given(userRepository.existsByEmail("exist@email.com")).willReturn(true); 

        // When & Then
        assertThatThrownBy(() -> userService.createUser("new_user", "pw", "exist@email.com"))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 존재하는 이메일입니다.");
    }
    
    //관리자 테스트는 생략
    @Test
    @DisplayName("로그인 성공: 아이디가 존재하고 비밀번호가 일치하면 유저 객체를 반환한다")
    void login_Success() {
        // Given
        String inputPassword = "password123";
        given(userRepository.findByName("tester")).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(inputPassword, testUser.getPassword())).willReturn(true); // 비번 일치

        // When
        User loggedInUser = userService.login("tester", inputPassword);

        // Then
        assertThat(loggedInUser).isNotNull();
        assertThat(loggedInUser.getName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("로그인 실패: 아이디가 존재하지 않으면 예외 발생")
    void login_Fail() {
        // Given
        given(userRepository.findByName("tester")).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.login("tester", "wrong"))
                .isInstanceOf(EntityNotFoundException.class);
    }

    @Test
    @DisplayName("로그인 실패: 아이디는 존재하지만 비밀번호가 일치하지 않으면 InvalidPasswordException 발생")
    void login_Fail_WrongPassword() {
        // Given
        String wrongPassword = "wrongPassword";
        given(userRepository.findByName("tester")).willReturn(Optional.of(testUser));
        // 비밀번호 불일치 설정
        given(passwordEncoder.matches(wrongPassword, testUser.getPassword())).willReturn(false); 

        // When & Then
        assertThatThrownBy(() -> userService.login("tester", wrongPassword))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("유저 조회 성공: ID로 유저를 찾으면 해당 유저 객체를 반환한다")
    void getUserById_Success() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        User result = userService.getUserById(1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getId()).isEqualTo(1L);
    }

    @Test
    @DisplayName("유저 조회 실패: 존재하지 않는 ID로 조회 시 EntityNotFoundException 발생한다")
    void getUserById_Fail_NotFound() {
        // Given
        given(userRepository.findById(99L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> userService.getUserById(99L))
                .isInstanceOf(EntityNotFoundException.class);
    }


    @Test
    @DisplayName("유저 이름 변경 성공: 중복되지 않은 이름으로 변경 시 DB에 반영된다")
    void updateUserName_Success() {
        // Given
        String newName = "new_tester";
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByName(newName)).willReturn(false); // 중복 아님

        // When
        User result = userService.updateUserName(newName, 1L);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo(newName);
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("이름 변경 실패: 변경하려는 이름이 이미 존재하면 예외가 발생한다")
    void updateUserName_Fail_Duplicate() {
        // Given
        String duplicateName = "exists_user";
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByName(duplicateName)).willReturn(true); // 중복

        // When & Then
        assertThatThrownBy(() -> userService.updateUserName(duplicateName, 1L))
                .isInstanceOf(DuplicateException.class)
                .hasMessage("이미 존재하는 아이디입니다.");
    }

    @Test
    @DisplayName("이름 변경: 현재와 동일한 이름으로 변경 요청 시 중복 예외가 발생하지 않고 통과해야 한다")
    void updateUserName_SameName_Success() {
        // Given
        String sameName = "tester"; // 현재 이름과 동일
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        
        // When
        User result = userService.updateUserName(sameName, 1L);

        // Then
        assertThat(result.getName()).isEqualTo(sameName);
    }


    @Test
    @DisplayName("비밀번호 변경 성공: 현재 비밀번호가 일치하면 새 비밀번호로 암호화하여 변경한다")
    void updatePassword_Success() {
        // Given
        String currentRawPw = "oldPw";
        String newRawPw = "newPw";
        String newEncodedPw = "newEncodedPw";

        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(currentRawPw, testUser.getPassword())).willReturn(true); // 구 비번 일치
        given(passwordEncoder.encode(newRawPw)).willReturn(newEncodedPw); // 신 비번 암호화

        // When
        User result = userService.updatePassword(currentRawPw, newRawPw, 1L);

        // Then
        assertThat(result.getPassword()).isEqualTo(newEncodedPw); // 변경 확인
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("비밀번호 변경 실패: 현재 비밀번호가 일치하지 않으면 예외가 발생한다")
    void updatePassword_Fail_WrongCurrentPassword() {
        // Given
        String wrongCurrentPw = "wrong";
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(passwordEncoder.matches(wrongCurrentPw, testUser.getPassword())).willReturn(false); // 불일치

        // When & Then
        assertThatThrownBy(() -> userService.updatePassword(wrongCurrentPw, "newPw", 1L))
                .isInstanceOf(InvalidPasswordException.class);
    }

    @Test
    @DisplayName("이메일 변경 성공: 변경된 객체를 반환한다")
    void updateEmail_Success() {
        // Given   
        String newEmail = "newEmail@email.com";


        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail(newEmail)).willReturn(false); // 불일치

        // When
        User result = userService.updateEmail(newEmail, 1L);

        // Then
        assertThat(result.getEmail()).isEqualTo(newEmail); // 변경 확인
        verify(userRepository).save(testUser);
    }

    @Test
    @DisplayName("이메일 변경 실패: 이미 존재하는 이메일이면 예외가 발생한다")
    void updateEmail_Fail() {
        String wrongEmail = "wrong";
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
        given(userRepository.existsByEmail(wrongEmail)).willReturn(true); // 불일치

        // When & Then
        assertThatThrownBy(() -> userService.updateEmail(wrongEmail, 1L))
                .isInstanceOf(DuplicateException.class);
    }
    

    @Test
    @DisplayName("회원 탈퇴 성공: 유저 상태를 비활성화(deleted=true)하고 저장한다")
    void deleteUser_Success() {
        // Given
        given(userRepository.findById(1L)).willReturn(Optional.of(testUser));

        // When
        userService.deleteUser(1L);

        // Then
        assertThat(testUser.isDeleted()).isTrue(); // Soft Delete 확인
        assertThat(testUser.getPassword()).isNotEqualTo("encodedPw"); // 비번 랜덤 변경 로직 확인
        verify(userRepository).save(testUser);
    }

    //private메서드들은 자동 검증됨 다른거 검증하면서
}