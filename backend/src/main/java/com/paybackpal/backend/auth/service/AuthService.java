package com.paybackpal.backend.auth.service;

import com.paybackpal.backend.auth.dto.RegisterRequest;
import com.paybackpal.backend.auth.dto.RegisterResponse;
import com.paybackpal.backend.common.exception.DuplicateResourceException;
import com.paybackpal.backend.user.entity.AppUser;
import com.paybackpal.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional
    public RegisterResponse register(RegisterRequest request) {
        String normalizedName = request.getName().trim();
        String normalizedEmail = request.getEmail().trim().toLowerCase();
        String normalizedPhoneNumber = request.getPhoneNumber().trim();
        String normalizedUpiId = normalizeOptional(request.getUpiId());

        if (userRepository.existsByEmail(normalizedEmail)) {
            throw new DuplicateResourceException("Email is already registered");
        }

        if (userRepository.existsByPhoneNumber(normalizedPhoneNumber)) {
            throw new DuplicateResourceException("Phone number is already registered");
        }

        String passwordHash = passwordEncoder.encode(request.getPassword());

        AppUser user = new AppUser(
                normalizedName,
                normalizedEmail,
                normalizedPhoneNumber,
                normalizedUpiId,
                passwordHash
        );

        try {
            AppUser savedUser = userRepository.saveAndFlush(user);
            return RegisterResponse.from(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("User with same email or phone number already exists");
        }
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
