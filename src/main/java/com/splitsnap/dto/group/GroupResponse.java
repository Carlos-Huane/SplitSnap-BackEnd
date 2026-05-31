package com.splitsnap.dto.group;

import com.splitsnap.model.Group;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter @Builder
public class GroupResponse {

    private UUID id;
    private String name;
    private String emoji;
    private UUID createdBy;
    private LocalDateTime createdAt;
    private int memberCount;
    private List<GroupMemberResponse> members;

    public static GroupResponse from(Group group) {
        List<GroupMemberResponse> members = group.getMembers().stream()
                .map(GroupMemberResponse::from)
                .toList();

        return GroupResponse.builder()
                .id(group.getId())
                .name(group.getName())
                .emoji(group.getEmoji())
                .createdBy(group.getCreatedBy().getId())
                .createdAt(group.getCreatedAt())
                .memberCount(members.size())
                .members(members)
                .build();
    }
}
