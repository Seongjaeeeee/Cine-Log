package com.cinelog.server.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.cinelog.server.domain.Role;
import com.cinelog.server.domain.User;
import com.cinelog.server.exception.EntityNotFoundException;
import com.cinelog.server.exception.security.InvalidPasswordException;
import com.cinelog.server.exception.user.DuplicateException;
import com.cinelog.server.global.error.ErrorCode;
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
    public User createUser(String userName,String password,String email){//회원가입->삭제된 회원으로 로그인 막아야됨 "탈퇴한회원" 못쓰게하기??
        validateUserName(userName);
        validateEmail(email);
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User(userName, encryptedPassword,email);
        userRepository.save(newUser);
        return newUser;
    }
    @Transactional
    public User createAdmin(String userName,String password,String email){
        validateUserName(userName);
        validateEmail(email);
        String encryptedPassword = passwordEncoder.encode(password);
        User newUser = new User(userName, encryptedPassword,email,Role.ADMIN);
        userRepository.save(newUser);
        return newUser;
    }

    public User login(String userName,String password){//로그인 - 탈퇴한회원 로그인 걱정 x jdbc단에서 막혀있음
        User user = getUserByUserName(userName);
        if(checkPassword(user,password)) return user;
        else throw new InvalidPasswordException(ErrorCode.INVALID_PASSWORD);
    }

    public User getUserById(Long id){
        return userRepository.findById(id)
            .orElseThrow(()->new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }

    //이 아래로직들은 전부 인가 된 상태라고 가정
    @Transactional
    public User updateUserName(String newName,Long id){
        User user = getUserById(id);
        if (!user.getName().equals(newName)) validateUserName(newName);
        user.changeName(newName);
        userRepository.save(user);
        return user;
    }
    @Transactional
    public User updatePassword(String currentPassword,String newPassword,Long id){
        User user = getUserById(id);
        if(!checkPassword(user, currentPassword))throw new InvalidPasswordException(ErrorCode.INVALID_PASSWORD);
        String encryptedPassword = passwordEncoder.encode(newPassword);
        user.changePassword(encryptedPassword);
        userRepository.save(user);
        return user;
    } 
    @Transactional
    public User updateEmail(String email, Long id){
        User user = getUserById(id);
        if (!user.getEmail().equals(email)) validateEmail(email);
        user.changeEmail(email);
        userRepository.save(user);
        return user;
    } 
    @Transactional
    public void deleteUser(Long id){//실제 삭제가 아닌 상태 변경 후 논리 삭제
        User user = getUserById(id);
        user.deactivate();
        userRepository.save(user);
    }

    private User getUserByUserName(String userName){
        return userRepository.findByName(userName).orElseThrow(() -> new EntityNotFoundException(ErrorCode.USER_NOT_FOUND));
    }
    
    private void validateUserName(String userName){
        if (userRepository.existsByName(userName)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_NAME);
        }
    }
    private void validateEmail(String email){
        if (userRepository.existsByEmail(email)) {
            throw new DuplicateException(ErrorCode.DUPLICATE_EMAIL);
        }
    }
    private boolean checkPassword(User user,String password){
        return passwordEncoder.matches(password,user.getPassword());
    }

}
