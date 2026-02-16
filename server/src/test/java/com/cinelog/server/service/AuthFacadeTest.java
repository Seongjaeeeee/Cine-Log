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
        testUser = new User("tester", "encodedPw", Role.USER);
        testUser.setId(1L);
        testSessionUser = new SessionUser(1L, "tester", Role.USER);
    }

    @Test
    @DisplayName("회원가입: UserService.createUser를 호출한다")
    void createUser_Success() {
        // When
        authFacade.createUser("new_user", "pw");
        // Then
        verify(userService).createUser("new_user", "pw");
    }
    //createAdmin()은 생략

    @Test
    @DisplayName("로그인 성공: UserService에서 유저를 받아와 세션에 저장한다")
    void login_Success() {
        // Given
        given(userService.login("tester", "pw")).willReturn(testUser);

        // When
        authFacade.login("tester", "pw");

        // Then
        verify(userService).login("tester", "pw");
        verify(sessionManager).saveLoginUser(any(SessionUser.class));//세션 유저로 변환해 저장했는지 확인
    }

    @Test
    @DisplayName("로그아웃 메서드를 호출한다")
    void logout_Success(){
        authFacade.logout();
        verify(sessionManager).logout();
    }

    @Test
    @DisplayName("이름 변경 성공: DB 업데이트 후 세션 정보도 동기화한다")
    void updateUserName_Success() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        String newName = "new_tester";

        // When
        authFacade.updateUserName(newName);

        // Then
        // 1. DB 업데이트 호출 (ID와 새 이름 전달)
        verify(userService).updateUserName(newName, 1L);
        // 2. 세션 업데이트 호출 
        verify(sessionManager).changeUserName(newName);
    }

    @Test
    @DisplayName("비밀번호 변경 성공: DB 업데이트 후 보안을 위해 로그아웃 처리한다")
    void updatePassword_Success() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.of(testSessionUser));
        String currentPw = "oldPw";
        String newPw = "newPw";

        // When
        authFacade.updatePassword(currentPw, newPw);

        // Then
        // 1. DB 비밀번호 변경 호출
        verify(userService).updatePassword(currentPw, newPw, 1L);
        // 2. 로그아웃 호출 
        verify(sessionManager).logout();
    }

    @Test
    @DisplayName("회원 탈퇴 성공: 유저 비활성화 후 로그아웃 처리한다")
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
    @DisplayName("유저 조회 실패: 로그인하지 않은 상태에서 유저 정보 요청 시 예외 발생")
    void getUser_Fail_NotLoggedIn() {
        // Given
        given(sessionManager.getLoginUser()).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> authFacade.getUser())
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("로그인 되지 않았습니다.");
    }
    
    @Test
    @DisplayName("로그인 상태 체크: 세션 매니저의 결과를 그대로 반환한다")
    void isLoggedIn_Check() {
        // Given
        given(sessionManager.isLoggedIn()).willReturn(true);
        // When
        boolean result = authFacade.isLoggedIn();
        // Then
        assertThat(result).isTrue();
    }
}
