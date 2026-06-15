package com.example.memberchat.domain.user;

import com.example.memberchat.api.CreateUserRequest;
import com.example.memberchat.api.UserResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;

    @Transactional
    public UserResponse createUser(CreateUserRequest request) {
        User user = User.builder()
                .nickname(request.nickname())
                .password(request.password())
                .username(request.username())
                .build();

        User savedUser = userRepository.save(user);

        return UserResponse.from(savedUser);
    }

    @Transactional(readOnly = true)
    public List<UserResponse> viewUsers() {
        return userRepository.findAll()
                .stream()
                .map(UserResponse::from)
                .toList();
    }
}
