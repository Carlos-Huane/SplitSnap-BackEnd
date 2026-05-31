package com.splitsnap.repository;

import com.splitsnap.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {

    // Expense.id es String, por eso el parámetro va como String
    List<ExpenseSplit> findByExpenseId(String expenseId);
}