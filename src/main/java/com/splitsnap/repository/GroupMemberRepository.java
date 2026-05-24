package com.splitsnap.repository;

import com.splitsnap.model.GroupMember;
import com.splitsnap.model.GroupMemberId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

public interface GroupMemberRepository extends JpaRepository<GroupMember, GroupMemberId> {

    @Query("SELECT COUNT(gm) > 0 FROM GroupMember gm WHERE gm.group.id = :groupId AND gm.user.id = :userId")
    boolean existsByGroupIdAndUserId(@Param("groupId") UUID groupId, @Param("userId") UUID userId);
}
