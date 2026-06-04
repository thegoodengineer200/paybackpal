package com.paybackpal.backend.auth.service;

import com.paybackpal.backend.auth.dto.LoginRequest;
import com.paybackpal.backend.auth.dto.LoginResponse;
import com.paybackpal.backend.auth.dto.RegisterRequest;
import com.paybackpal.backend.auth.dto.RegisterResponse;
import com.paybackpal.backend.auth.jwt.JwtService;
import com.paybackpal.backend.common.exception.DuplicateResourceException;
import com.paybackpal.backend.user.entity.AppUser;
import com.paybackpal.backend.user.repository.UserRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            JwtService jwtService
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
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
            AppUser savedUser = userRepository.save(user);
            return RegisterResponse.from(savedUser);
        } catch (DataIntegrityViolationException exception) {
            throw new DuplicateResourceException("User with same email or phone number already exists");
        }
    }

    @Transactional(readOnly = true)
    public LoginResponse login(LoginRequest request) {
        String normalizedEmail = request.getEmail().trim().toLowerCase();

        AppUser user = userRepository.findByEmail(normalizedEmail)
                .orElseThrow(() -> new BadCredentialsException("Invalid email or password"));

        if (!user.isActive()) {
            throw new BadCredentialsException("Invalid email or password");
        }

        boolean passwordMatches = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());

        if (!passwordMatches) {
            throw new BadCredentialsException("Invalid email or password");
        }

        String token = jwtService.generateToken(user);

        return LoginResponse.from(
                user, token, jwtService.getExpirationSeconds()
        );
    }

    private String normalizeOptional(String value) {
        if (value == null || value.isBlank()) {
            return null;
        }

        return value.trim();
    }
}
