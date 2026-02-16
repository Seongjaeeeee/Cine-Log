package com.cinelog.server.service;

import com.cinelog.server.domain.Role; // Role enum 위치에 맞게 임포트 필요
import com.cinelog.server.dto.user.SessionUser;
import com.cinelog.server.exception.security.UnAuthorizedException;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SessionManagerTest {

    @Mock
    private HttpServletRequest request;
    @Mock
    private HttpSession session;
    @InjectMocks
    private SessionManager sessionManager;
    private SessionUser testUser;
    private static final String LOGIN_USER = "loginUser";

    @BeforeEach
    void setUp() {
        testUser = new SessionUser(1L, "tester", Role.USER);
    }

    @Test
    @DisplayName("로그인 성공 시: 기존 세션을 파기하고 새 세션을 생성하여 유저를 저장한다 (세션 고정 보호)")
    void saveLoginUser_Success() {
        // Given
        HttpSession oldSession = mock(HttpSession.class); // 기존에 있던 세션
        HttpSession newSession = mock(HttpSession.class); // 새로 만들 세션
        given(request.getSession(false)).willReturn(oldSession);
        given(request.getSession(true)).willReturn(newSession);

        // When
        sessionManager.saveLoginUser(testUser);

        // Then
        verify(oldSession).invalidate(); // 1. 기존 세션 파기 확인
        verify(newSession).setAttribute(eq(LOGIN_USER), any(SessionUser.class)); // 2. 새 세션에 저장 확인
    }

    @Test
    @DisplayName("로그아웃 성공 시: 세션이 존재하면 무효화(invalidate) 한다")
    void logout_Success() {
        // Given
        given(request.getSession(false)).willReturn(session);
        // When
        sessionManager.logout();
        // Then
        verify(session).invalidate();
    }

    @Test
    @DisplayName("이름 변경 성공: 세션에 저장된 유저 객체의 이름을 변경하여 다시 저장한다")
    void changeUserName_Success() {
        // Given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute(LOGIN_USER)).willReturn(testUser);
        String newName = "newTester";

        // When
        sessionManager.changeUserName(newName);

        // Then
        // session.setAttribute가 호출되었는지, 그리고 저장된 객체의 이름이 바뀌었는지 검증
        verify(session).setAttribute(eq(LOGIN_USER), argThat(user -> 
            user instanceof SessionUser && ((SessionUser) user).getName().equals(newName)
        ));
    }

    @Test
    @DisplayName("이름 변경 실패: 로그인하지 않은 상태에서 변경 시도 시 예외가 발생한다")
    void changeUserName_Fail_NotLoggedIn() {
        // Given
        given(request.getSession(false)).willReturn(null); // 로그인 안 함

        // When & Then
        assertThatThrownBy(() -> sessionManager.changeUserName("newName"))
                .isInstanceOf(UnAuthorizedException.class)
                .hasMessage("로그인 상태가 아닙니다.");
    }

    @Test
    @DisplayName("로그인 체크 (True): 세션이 있고 유저 정보가 있으면 true를 반환한다")
    void isLoggedIn_True() {
        // Given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute(LOGIN_USER)).willReturn(testUser);

        // When
        boolean result = sessionManager.isLoggedIn();

        // Then
        assertThat(result).isTrue();
    }

    @Test
    @DisplayName("로그인 체크 (False): 세션이 아예 없으면 false를 반환한다")
    void isLoggedIn_False_NoSession() {
        // Given
        given(request.getSession(false)).willReturn(null); // 세션 없음

        // When
        boolean result = sessionManager.isLoggedIn();

        // Then
        assertThat(result).isFalse();
    }

    @Test
    @DisplayName("로그인 유저 조회 성공: 세션에서 유저 정보를 Optional로 반환한다")
    void getLoginUser_Success() {
        // Given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute(LOGIN_USER)).willReturn(testUser);

        // When
        Optional<SessionUser> result = sessionManager.getLoginUser();

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("tester");
    }

    @Test
    @DisplayName("로그인 유저 조회 실패: 세션은 있지만 유저 속성이 없으면 Empty를 반환한다")
    void getLoginUser_Empty_NoAttribute() {
        // Given
        given(request.getSession(false)).willReturn(session);
        given(session.getAttribute(LOGIN_USER)).willReturn(null); // 세션은 있는데 값은 없음

        // When
        Optional<SessionUser> result = sessionManager.getLoginUser();

        // Then
        assertThat(result).isEmpty();
    }

}