package com.splitsnap.service;

import com.splitsnap.dto.group.AddMemberRequest;
import com.splitsnap.dto.group.CreateGroupRequest;
import com.splitsnap.dto.group.GroupResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.exception.EntityNotFoundException;
import com.splitsnap.model.Group;
import com.splitsnap.model.GroupMember;
import com.splitsnap.model.GroupMemberId;
import com.splitsnap.model.User;
import com.splitsnap.repository.GroupMemberRepository;
import com.splitsnap.repository.GroupRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class GroupService {

    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final UserService userService;

    @Transactional
    public GroupResponse createGroup(CreateGroupRequest request, User creator) {
        Group group = Group.builder()
                .name(request.getName())
                .emoji(request.getEmoji() != null && !request.getEmoji().isBlank()
                        ? request.getEmoji() : "📦")
                .createdBy(creator)
                .build();

        groupRepository.save(group);

        addMemberToGroup(group, creator);

        if (request.getMemberIds() != null) {
            for (UUID memberId : request.getMemberIds()) {
                if (!memberId.equals(creator.getId())) {
                    User member = userService.findById(memberId);
                    addMemberToGroup(group, member);
                }
            }
        }

        return GroupResponse.from(groupRepository.findById(group.getId()).orElseThrow());
    }

    public List<GroupResponse> getMyGroups(UUID userId) {
        return groupRepository.findGroupsByMemberId(userId).stream()
                .map(GroupResponse::from)
                .toList();
    }

    public GroupResponse getGroupById(UUID groupId, UUID requestingUserId) {
        Group group = findById(groupId);
        checkMembership(group, requestingUserId);
        return GroupResponse.from(group);
    }

    @Transactional
    public GroupResponse addMember(UUID groupId, AddMemberRequest request, UUID requestingUserId) {
        Group group = findById(groupId);
        checkMembership(group, requestingUserId);

        User newMember = userService.findById(request.getUserId());

        if (groupMemberRepository.existsByGroupIdAndUserId(groupId, newMember.getId())) {
            throw new BusinessException("El usuario ya es miembro del grupo");
        }

        addMemberToGroup(group, newMember);
        return GroupResponse.from(groupRepository.findById(groupId).orElseThrow());
    }

    @Transactional
    public void removeMember(UUID groupId, UUID targetUserId, UUID requestingUserId) {
        Group group = findById(groupId);

        if (!group.getCreatedBy().getId().equals(requestingUserId)) {
            throw new BusinessException("Solo el creador del grupo puede remover miembros");
        }
        if (targetUserId.equals(requestingUserId)) {
            throw new BusinessException("El creador no puede removerse a sí mismo");
        }
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, targetUserId)) {
            throw new EntityNotFoundException("El usuario no es miembro de este grupo");
        }

        groupMemberRepository.deleteById(new GroupMemberId(groupId, targetUserId));
    }

    private void addMemberToGroup(Group group, User user) {
        GroupMember member = GroupMember.builder()
                .id(new GroupMemberId(group.getId(), user.getId()))
                .group(group)
                .user(user)
                .build();
        groupMemberRepository.save(member);
    }

    private void checkMembership(Group group, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(group.getId(), userId)) {
            throw new BusinessException("No tienes acceso a este grupo");
        }
    }

    public Group findById(UUID groupId) {
        return groupRepository.findById(groupId)
                .orElseThrow(() -> new EntityNotFoundException("Grupo no encontrado"));
    }
}
