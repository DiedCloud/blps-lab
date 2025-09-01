package com.example.blps.service;

import com.example.blps.dao.repository.UserRepository;
import com.example.blps.dao.repository.model.User;
import com.example.blps.security.UserDetailsImpl;
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
        return new UserDetailsImpl(userRepository.findByLogin(username));
    }

    public User getByLogin(String login) {
        User user = userRepository.findByLogin(login);
        if (user == null) {
            throw new UsernameNotFoundException("User not found");
        }
        return user;
    }

    public User getCurrentUser() {
        return getByLogin(SecurityContextHolder.getContext().getAuthentication().getName());
    }

    public User createUser(String login, String password, String name) {
        User user = new User();
        user.setLogin(login);
        user.setPassword(password);
        user.setName(name);
        userRepository.save(user);
        return user;
    }
}
