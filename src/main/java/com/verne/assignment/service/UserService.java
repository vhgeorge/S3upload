package com.verne.assignment.service;


import com.verne.assignment.model.User;

public interface UserService {
    User save(User user);
    User findByUsername(String username);
}
