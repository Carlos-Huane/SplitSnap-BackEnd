package com.splitsnap.repository;

import com.splitsnap.model.GroupMember;
import com.splitsnap.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    boolean existsByGroupIdAndUserId(UUID groupId, UUID userId);
}
