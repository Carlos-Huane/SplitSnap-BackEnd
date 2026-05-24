package com.splitsnap.dto.expense;

import com.splitsnap.model.Expense;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDate;
import java.util.UUID;

@Getter @Builder
public class ExpenseResponse {

    private UUID id;
    private String description;
    private Double amount;
    private UUID groupId;
    private UUID paidBy;
    private LocalDate expenseDate;

    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .groupId(expense.getGroup().getId())
                .paidBy(expense.getPaidBy().getId())
                .expenseDate(expense.getExpenseDate())
                .build();
    }
}