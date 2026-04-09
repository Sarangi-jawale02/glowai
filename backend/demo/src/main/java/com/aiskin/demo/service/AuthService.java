package com.aiskin.demo.service;

import com.aiskin.demo.dto.SigninRequest;
import com.aiskin.demo.dto.SignupRequest;
import com.aiskin.demo.entity.User;
import com.aiskin.demo.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    public String signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            return "Email already exists";
        }

        User user = new User();
        user.setFirstName(request.getFirstName());
        user.setLastName(request.getLastName());
        user.setEmail(request.getEmail());
        user.setPassword(request.getPassword()); // Storing raw password as per requirement

        userRepository.save(user);
        return "Signup successful";
    }

    public String signin(SigninRequest request) {
        Optional<User> userOptional = userRepository.findByEmail(request.getEmail());

        if (userOptional.isPresent()) {
            User user = userOptional.get();
            if (user.getPassword().equals(request.getPassword())) {
                return "Login successful";
            } else {
                return "Invalid password";
            }
        }
        return "User not found";
    }
}
