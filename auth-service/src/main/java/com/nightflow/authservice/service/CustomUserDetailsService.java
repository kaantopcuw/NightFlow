package com.nightflow.authservice.service;

import com.nightflow.authservice.entity.User;
import com.nightflow.authservice.repository.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public CustomUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }


    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(username).orElseThrow(()-> new UsernameNotFoundException("Kullanıcı bulunamadı: " + username));


        return org.springframework.security.core.userdetails.
                User.builder().username(user.getUsername())
                .password(user.getPassword())
                .roles(user.getRole())
                .build();
    }
}
