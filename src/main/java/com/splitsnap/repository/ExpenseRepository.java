package com.splitsnap.repository;

import com.splitsnap.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // LOGICA SCRUM-98: Buscar gastos de un grupo ordenados por fecha de creación descendente
    List<Expense> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    @Query("SELECT e FROM Expense e WHERE e.paidBy.id = :paidById AND e.group.id = :groupId")
    List<Expense> findByPaidByIdAndGroupId(@Param("paidById") UUID paidById, @Param("groupId") UUID groupId);

    @Query("SELECT e FROM Expense e WHERE e.paidBy.id = :paidById")
    List<Expense> findByPaidById(@Param("paidById") UUID paidById);
}