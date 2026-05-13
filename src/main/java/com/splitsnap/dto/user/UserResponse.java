package com.splitsnap.dto.user;

import com.splitsnap.model.User;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.UUID;

@Getter @Builder
public class UserResponse {
    private UUID id;
    private String name;
    private String email;
    private String phone;
    private String avatarUrl;
    private BigDecimal credits;

    public static UserResponse from(User user) {
        return UserResponse.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .phone(user.getPhone())
                .avatarUrl(user.getAvatarUrl())
                .credits(user.getCredits())
                .build();
    }
}
