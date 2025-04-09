package com.example.blps.service;

import com.example.blps.dao.repository.UserRepository;
import com.example.blps.dao.repository.mapper.UserMapper;
import com.example.blps.entity.User;
import lombok.AllArgsConstructor;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@AllArgsConstructor
public class UserService implements UserDetailsService {
    private final UserRepository userRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        return UserMapper.getUser(userRepository.findByLogin(username));
    }

    public User getByLogin(String login) {
        com.example.blps.dao.repository.model.User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return UserMapper.getUser(user);
    }

    public User getCurrentUser() {
        return getByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public User createUser(String login, String password, String name) {
        User user = new User(login, password, name);
        userRepository.save(UserMapper.toUserRepoEntity(user));
        return user;
    }
}
