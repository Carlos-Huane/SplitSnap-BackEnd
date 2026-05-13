package com.splitsnap.dto.group;

import com.splitsnap.model.GroupMember;
import lombok.Builder;
import lombok.Getter;

import java.util.UUID;

@Getter @Builder
public class GroupMemberResponse {

    private UUID id;
    private String name;
    private String email;
    private String avatarUrl;

    public static GroupMemberResponse from(GroupMember gm) {
        return GroupMemberResponse.builder()
                .id(gm.getUser().getId())
                .name(gm.getUser().getName())
                .email(gm.getUser().getEmail())
                .avatarUrl(gm.getUser().getAvatarUrl())
                .build();
    }
}
