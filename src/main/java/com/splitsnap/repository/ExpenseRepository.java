package com.splitsnap.repository;

import com.splitsnap.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseRepository extends JpaRepository<Expense, String> {
    // Busca gastos creados por el usuario
    List<Expense> findByCreatedById(UUID userId);

    // Busca gastos creados por el usuario en un grupo específico
    List<Expense> findByCreatedByIdAndGroupId(UUID userId, UUID groupId);
}