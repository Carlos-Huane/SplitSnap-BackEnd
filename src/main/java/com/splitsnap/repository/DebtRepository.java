package com.splitsnap.repository;

import com.splitsnap.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    @Query("SELECT d FROM Debt d JOIN FETCH d.fromUser JOIN FETCH d.toUser WHERE d.group.id = :groupId")
    List<Debt> findByGroupId(@Param("groupId") UUID groupId);

    @Query("SELECT d FROM Debt d JOIN FETCH d.fromUser JOIN FETCH d.toUser WHERE d.group.id = :groupId AND d.status = :status")
    List<Debt> findByGroupIdAndStatus(@Param("groupId") UUID groupId, @Param("status") String status);

    // NUEVO MÉTODO PARA HU-5.2
    @Query("SELECT d FROM Debt d JOIN FETCH d.fromUser JOIN FETCH d.toUser WHERE d.id = :debtId AND d.group.id = :groupId")
    Optional<Debt> findByIdAndGroupId(@Param("debtId") String debtId, @Param("groupId") UUID groupId);
}