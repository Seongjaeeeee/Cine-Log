package com.cinelog.server.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;
import com.cinelog.server.exception.security.InvalidPasswordException;
import com.cinelog.server.exception.user.DuplicateUserNameException;
import com.cinelog.server.exception.user.UserNotFoundException;
import com.cinelog.server.repository.UserRepository;

@Service
@Transactional(readOnly = true)
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    public UserService(UserRepository userRepository,PasswordEncoder passwordEncoder){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }
    @Transactional
    public void createUser(String userName,String password){//회원가입->삭제된 회원으로 로그인 막아야됨 "탈퇴한회원" 못쓰게하기??
        validateUserName(userName);
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User(userName, encryptedPassword);
        userRepository.save(newUser);
    }
    @Transactional
    public void createAdmin(String userName,String password){
        validateUserName(userName);
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User(userName, encryptedPassword,Role.ADMIN);
        userRepository.save(newUser);
    }

    public User login(String userName,String password){//로그인 - 탈퇴한회원 로그인 걱정 x jdbc단에서 막혀있음
        User user = getUserByUserName(userName);
        if(checkPassword(user,password))return user;
        else throw new InvalidPasswordException();
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
            .orElseThrow(()->new UserNotFoundException("해당 id로 유저를 찾을 수 없습니다"));
    }

    //이 아래로직들은 전부 인가 된 상태라고 가정
    @Transactional
    public boolean updateUserName(String newName,Long id){
        User user = getUserById(id);
        validateUserName(newName);
        user.changeName(newName);
        userRepository.save(user);
        return true;
    }
    @Transactional
    public void updatePassword(String currentPassword,String newPassword,Long id){
        User user = getUserById(id);
        if(!checkPassword(user, currentPassword))throw new InvalidPasswordException();
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encryptedPassword);
        userRepository.save(user);
    } 
    @Transactional
    public void deleteUser(Long id){//실제 삭제가 아닌 상태 변경 후 논리 삭제
        User user = getUserById(id);
        user.deactivate();
        userRepository.save(user);
    }

    private User getUserByUserName(String userName){
        return userRepository.findByName(userName).orElseThrow(() -> new UserNotFoundException("존재하지 않는 아이디입니다."));
    }
    
    private void validateUserName(String userName){
        if (userRepository.existsByName(userName)) {
            throw new DuplicateUserNameException("이미 존재하는 아이디입니다.");
        }
    }
    private boolean checkPassword(User user,String password){
        return passwordEncoder.matches(password,user.getPassword());
    }

}
