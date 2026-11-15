package com.civicbuild.service.impl;

import com.civicbuild.dto.request.LoginRequest;
import com.civicbuild.dto.request.RegisterRequest;
import com.civicbuild.dto.response.AuthResponse;
import com.civicbuild.entity.Role;
import com.civicbuild.entity.User;
import com.civicbuild.enums.UserRole;
import com.civicbuild.exception.BadRequestException;
import com.civicbuild.exception.UnauthorizedException;
import com.civicbuild.repository.RoleRepository;
import com.civicbuild.repository.UserRepository;
import com.civicbuild.security.CustomUserDetails;
import com.civicbuild.security.JwtUtils;
import com.civicbuild.service.AuthService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthServiceImpl implements AuthService {

    @Autowired
    private AuthenticationManager authenticationManager;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtils jwtUtils;

    @Override
    @Transactional
    public AuthResponse login(LoginRequest loginRequest) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(loginRequest.getUsername(), loginRequest.getPassword())
        );

        SecurityContextHolder.getContext().setAuthentication(authentication);
        String jwt = jwtUtils.generateJwtToken(authentication);

        CustomUserDetails userDetails = (CustomUserDetails) authentication.getPrincipal();
        return new AuthResponse(
                jwt,
                userDetails.getId(),
                userDetails.getUsername(),
                userDetails.getEmail(),
                userDetails.getFullName(),
                userDetails.getRole(),
                userDetails.getDepartmentCode()
        );
    }

    @Override
    @Transactional
    public AuthResponse register(RegisterRequest registerRequest) {
        if (userRepository.existsByUsername(registerRequest.getUsername())) {
            throw new BadRequestException("Username is already taken");
        }

        if (userRepository.existsByEmail(registerRequest.getEmail())) {
            throw new BadRequestException("Email is already in use");
        }

        UserRole targetRole = registerRequest.getRole() != null ? registerRequest.getRole() : UserRole.ROLE_APPLICANT;
        Role role = roleRepository.findByName(targetRole)
                .orElseGet(() -> roleRepository.save(new Role(targetRole, targetRole.name())));

        User user = new User(
                registerRequest.getUsername(),
                registerRequest.getEmail(),
                passwordEncoder.encode(registerRequest.getPassword()),
                registerRequest.getFullName(),
                registerRequest.getPhone(),
                role,
                registerRequest.getDepartmentCode()
        );

        user = userRepository.save(user);

        String jwt = jwtUtils.generateTokenFromUsername(user.getUsername());
        return new AuthResponse(
                jwt,
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getRole().getName().name(),
                user.getDepartmentCode()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            throw new UnauthorizedException("User is not authenticated");
        }

        String username = authentication.getName();
        return userRepository.findByUsername(username)
                .or(() -> userRepository.findByEmail(username))
                .orElseThrow(() -> new UnauthorizedException("User not found: " + username));
    }
}
