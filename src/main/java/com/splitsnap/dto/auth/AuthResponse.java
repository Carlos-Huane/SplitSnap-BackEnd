package com.splitsnap.dto.auth;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter @Builder @AllArgsConstructor
public class AuthResponse {
    private String token;
    private UserInfo user;

    @Getter @Builder @AllArgsConstructor
    public static class UserInfo {
        private UUID id;
        private String name;
        private String email;
        private String phone;
        private String avatarUrl;
    }
}
