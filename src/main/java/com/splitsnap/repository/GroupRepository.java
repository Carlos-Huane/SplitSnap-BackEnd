package com.splitsnap.repository;

import com.splitsnap.model.Group;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.UUID;

public interface GroupRepository extends JpaRepository<Group, UUID> {

    @Query("SELECT g FROM Group g JOIN g.members m WHERE m.user.id = :userId ORDER BY g.createdAt DESC")
    List<Group> findGroupsByMemberId(@Param("userId") UUID userId);
}
