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

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;
import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.dto.user.UserResponse;
import com.cinelog.server.exception.security.UnAuthorizedException;

@ExtendWith(MockitoExtension.class)
class AuthFacadeTest {

    @Mock
    private UserService userService;
    @Mock
    private SessionManager sessionManager;
    @InjectMocks
    private AuthFacade authFacade;

    private User testUser;
    private SessionUser testSessionUser;

    @BeforeEach
    void setUp() {
        testUser = new User("tester", "encodedPw", "email@test.com", Role.USER);
        testUser.setId(1L);
        testSessionUser = new SessionUser(1L, "tester", Role.USER);
    }

    @Test
    @DisplayName("회원가입: UserService를 호출하고 결과를 UserResponse로 반환한다")
    void createUser_Success() {
        // Given
        given(userService.createUser("new_user", "pw", "email@test.com")).willReturn(testUser);

        // When
        UserResponse result = authFacade.createUser("new_user", "pw", "email@test.com");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo(testUser.getName());
        verify(userService).createUser("new_user", "pw", "email@test.com");
    }

    @Test
    @DisplayName("관리자 생성: UserService.createAdmin을 호출하고 결과를 반환한다")
    void createAdmin_Success() {
        // Given
        User adminUser = new User("admin", "pw", "admin@test.com", Role.ADMIN);
        given(userService.createAdmin("admin", "pw", "admin@test.com")).willReturn(adminUser);

        // When
        UserResponse result = authFacade.createAdmin("admin", "pw", "admin@test.com");

        // Then
        assertThat(result.role()).isEqualTo(Role.ADMIN);
        verify(userService).createAdmin("admin", "pw", "admin@test.com");
    }

    @Test
    @DisplayName("로그인 성공: 유저를 조회해 세션에 저장하고 UserResponse를 반환한다")
    void login_Success() {
        // Given
        given(userService.login("tester", "pw")).willReturn(testUser);

        // When
        UserResponse result = authFacade.login("tester", "pw");

        // Then
        assertThat(result).isNotNull();
        assertThat(result.userName()).isEqualTo("tester");
        verify(userService).login("tester", "pw");
        verify(sessionManager).saveLoginUser(any(SessionUser.class)); // 세션 저장 확인
    }

    @Test
    @DisplayName("로그아웃 메서드를 호출한다")
    void logout_Success(){
        authFacade.logout();
        verify(sessionManager).logout();
    }

    @Test
    @DisplayName("내 정보 조회: 세션 ID로 UserService를 조회하여 반환한다")
    void getMeById_Success() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        given(userService.getUserById(1L)).willReturn(testUser);

        // When
        UserResponse result = authFacade.getMeById();

        // Then
        assertThat(result.id()).isEqualTo(1L);
        verify(userService).getUserById(1L);
    }

    @Test
    @DisplayName("이름 변경: DB 업데이트 후 세션 정보를 동기화하고 결과를 반환한다")
    void updateUserName_Success() {
        // Given
        String newName = "new_tester";
        User updatedUser = new User(newName, "pw", "email", Role.USER); 
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        given(userService.updateUserName(newName, 1L)).willReturn(updatedUser);

        // When
        UserResponse result = authFacade.updateUserName(newName);

        // Then
        assertThat(result.userName()).isEqualTo(newName); // 바뀐 이름이 반환되는지 확인
        verify(userService).updateUserName(newName, 1L);
        verify(sessionManager).changeUserName(newName); // 세션 동기화 확인
    }

    @Test
    @DisplayName("비밀번호 변경: DB 업데이트 후 보안을 위해 로그아웃 처리한다")
    void updatePassword_Success() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        given(userService.updatePassword("old", "new", 1L)).willReturn(testUser);

        // When
        authFacade.updatePassword("old", "new");

        // Then
        verify(userService).updatePassword("old", "new", 1L);
        verify(sessionManager).logout(); // 로그아웃 호출 확인
    }

    @Test
    @DisplayName("이메일 변경: DB 업데이트를 호출하고 결과를 반환한다")
    void updateEmail_Success() {
        // Given
        String newEmail = "new@email.com";
        User updatedUser = new User("tester", "pw", newEmail, Role.USER);

        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        given(userService.updateEmail(newEmail, 1L)).willReturn(updatedUser);

        // When
        UserResponse result = authFacade.updateEmail(newEmail);

        // Then
        assertThat(result.email()).isEqualTo(newEmail);
        verify(userService).updateEmail(newEmail, 1L);
    }

    @Test
    @DisplayName("회원 탈퇴: 유저 삭제 로직 호출 후 로그아웃 처리한다")
    void deleteUser_Success() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));

        // When
        authFacade.deleteUser();

        // Then
        verify(userService).deleteUser(1L);
        verify(sessionManager).logout();
    }

    @Test
    @DisplayName("유저 조회 성공: 세션에 로그인 된 유저가 있으면 해당 정보를 반환한다")
    void getUser_Success() {
        //Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));

        // When
        SessionUser result = authFacade.getUser();

        // Then
        assertThat(result).isEqualTo(testSessionUser);
    }

    @Test
    @DisplayName("유저 조회 실패: 로그인하지 않은 상태에서 유저 정보 요청 시 예외가 발생한다")
    void getUser_Fail_NotLoggedIn() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.empty()); // 세션 비어있음

        // When & Then
        assertThatThrownBy(() -> authFacade.getUser())
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("로그인 되지 않았습니다.");
    }

    @Test
    void isLoggedIn_Check() {
        given(sessionManager.isLoggedIn()).willReturn(true);
        assertThat(authFacade.isLoggedIn()).isTrue();
    }
}