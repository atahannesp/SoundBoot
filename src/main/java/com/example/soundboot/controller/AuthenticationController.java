package com.example.soundboot.controller;

import com.example.soundboot.dto.request.LoginRequest;
import com.example.soundboot.dto.request.RegisterRequest;
import com.example.soundboot.entity.UserEntity;
import com.example.soundboot.repository.UserRepository;
import com.example.soundboot.security.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.bind.annotation.*;

/**
 * Controller class for handling user authentication and registration.
 * Provides endpoints for signing in and signing up users.
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationManager authenticationManager;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtils;

    /**
     * Authenticates a user based on their credentials and generates a JWT token.
     *
     * @param request A {@link LoginRequest} containing the username and password.
     * @return A generated JWT string if authentication is successful.
     */
    @PostMapping("/signin")
    public String authenticateUser(@RequestBody LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getUsername(),
                        request.getPassword()
                )
        );
        UserDetails userDetails = (UserDetails) authentication.getPrincipal();
        assert userDetails != null;
        return jwtUtils.generateToken(userDetails.getUsername());
    }

    /**
     * Registers a new user into the system.
     * Checks if the username already exists and encodes the password before saving.
     *
     * @param request A {@link RegisterRequest} containing the desired username and password.
     * @return A success message if registration is complete, or an error message if the user already exists.
     */
    @PostMapping("/signup")
    public String registerUser(@RequestBody RegisterRequest request) {
        if (userRepository.findByUsername(request.getUsername()).isPresent()) {
            return "User already exists";
        }
        UserEntity user = new UserEntity();
        user.setUsername(request.getUsername());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        userRepository.save(user);
        return "User registered successfully";
    }
}
