package com.splitsnap.repository;

import com.splitsnap.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {
    
    // LÓGICA SCRUM-99: Buscar todos los desgloses asociados a un gasto específico
    List<ExpenseSplit> findByExpenseId(UUID expenseId);
}