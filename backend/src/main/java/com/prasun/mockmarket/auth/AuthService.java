package com.prasun.mockmarket.auth;

import com.prasun.mockmarket.account.AccountService;
import com.prasun.mockmarket.common.ApiException;
import com.prasun.mockmarket.user.User;
import com.prasun.mockmarket.user.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AuthService {
    private final UserRepository users;
    private final AccountService accounts;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthService(UserRepository users, AccountService accounts, PasswordEncoder passwordEncoder, JwtService jwtService) {
        this.users = users;
        this.accounts = accounts;
        this.passwordEncoder = passwordEncoder;
        this.jwtService = jwtService;
    }

    @Transactional
    public AuthResponse register(RegisterRequest request) {
        var email = request.email().toLowerCase();
        if (users.existsByEmail(email)) {
            throw new ApiException(HttpStatus.BAD_REQUEST, "An account with this email already exists.");
        }
        var user = users.save(new User(request.name().trim(), email, passwordEncoder.encode(request.password())));
        accounts.createFor(user);
        return response(user);
    }

    public AuthResponse login(LoginRequest request) {
        var user = users.findByEmail(request.email().toLowerCase())
                .orElseThrow(() -> new BadCredentialsException("Invalid credentials"));
        if (!passwordEncoder.matches(request.password(), user.getPasswordHash())) {
            throw new BadCredentialsException("Invalid credentials");
        }
        return response(user);
    }

    public UserProfile profile(User user) {
        return new UserProfile(user.getId(), user.getName(), user.getEmail());
    }

    private AuthResponse response(User user) {
        return new AuthResponse(jwtService.createToken(user), profile(user));
    }
}
