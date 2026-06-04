package com.paybackpal.backend.auth.service;

import com.paybackpal.backend.user.entity.AppUser;
import com.paybackpal.backend.user.repository.UserRepository;
import org.springframework.security.authentication.AuthenticationCredentialsNotFoundException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CurrentUserService {

    private final UserRepository userRepository;

    public CurrentUserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public AppUser getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new AuthenticationCredentialsNotFoundException("User is not authenticated");
        }

        String email = authentication.getName(); // email of the current logged in user

        return userRepository.findByEmail(email)
                .orElseThrow(() ->
                        new AuthenticationCredentialsNotFoundException("Authenticated user not found")
                );
    }
}