package com.splitsnap.repository;

import com.splitsnap.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    @Query("SELECT d FROM Debt d JOIN FETCH d.fromUser JOIN FETCH d.toUser WHERE d.group.id = :groupId")
    List<Debt> findByGroupId(@Param("groupId") UUID groupId); // Cambiado a UUID

    @Query("SELECT d FROM Debt d JOIN FETCH d.fromUser JOIN FETCH d.toUser WHERE d.group.id = :groupId AND d.status = :status")
    List<Debt> findByGroupIdAndStatus(@Param("groupId") UUID groupId, @Param("status") String status);
}