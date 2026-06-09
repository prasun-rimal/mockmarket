package com.prasun.mockmarket.auth;

public record AuthResponse(String token, UserProfile user) {
}
