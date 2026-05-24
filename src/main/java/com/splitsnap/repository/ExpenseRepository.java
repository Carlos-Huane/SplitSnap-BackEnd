package com.splitsnap.repository;

import com.splitsnap.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    @Query("SELECT e FROM Expense e WHERE e.paidBy.id = :paidById AND e.group.id = :groupId")
    List<Expense> findByPaidByIdAndGroupId(@Param("paidById") UUID paidById, @Param("groupId") UUID groupId);

    @Query("SELECT e FROM Expense e WHERE e.paidBy.id = :paidById")
    List<Expense> findByPaidById(@Param("paidById") UUID paidById);
}