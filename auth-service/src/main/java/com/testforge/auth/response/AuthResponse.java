package com.testforge.auth.response;

public record AuthResponse(String message, String token, String username, String role) {}
