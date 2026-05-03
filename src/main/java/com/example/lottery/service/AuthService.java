package com.example.lottery.service;

import com.example.lottery.exception.ServiceException;
import com.example.lottery.model.User;
import com.example.lottery.repository.UserRepository;
import com.example.lottery.util.JwtUtil;
import com.example.lottery.util.PasswordEncoder;
import com.example.lottery.exception.ServiceException;

import javax.sql.DataSource;
import java.sql.SQLException;

public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;

    public AuthService(DataSource dataSource, JwtUtil jwtUtil) {
        this.userRepository = new UserRepository(dataSource);
        this.jwtUtil = jwtUtil;
    }

    public User register(String username, String password) throws ServiceException {
        try {
            if (userRepository.existsByUsername(username)) {
                throw new ServiceException("Username already exists");
            }
            User user = new User();
            user.setUsername(username);
            user.setPasswordHash(PasswordEncoder.encode(password));
            user.setRole("USER");
            return userRepository.save(user);
        } catch (SQLException e) {
            throw new ServiceException("Registration failed", e);
        }
    }

    public String login(String username, String password) throws ServiceException {
        try {
            User user = userRepository.findByUsername(username);
            if (user == null || !PasswordEncoder.matches(password, user.getPasswordHash())) {
                throw new ServiceException("Invalid credentials");
            }
            return jwtUtil.generateToken(user.getId(), user.getUsername(), user.getRole());
        } catch (SQLException e) {
            throw new ServiceException("Login failed", e);
        }
    }
}