package com.splitsnap.service;

import com.splitsnap.dto.transaction.TransactionHistoryDTO;
import com.splitsnap.model.Expense;
import com.splitsnap.model.Debt;
import com.splitsnap.model.User;
import com.splitsnap.repository.DebtRepository;
import com.splitsnap.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TransactionService {

    private final ExpenseRepository expenseRepository;
    private final DebtRepository debtRepository;

    public List<TransactionHistoryDTO> getHistory(User user, String groupId, String type) {
        List<TransactionHistoryDTO> list = new ArrayList<>();
        UUID userId = user.getId();
        UUID groupUuid = (groupId != null) ? UUID.fromString(groupId) : null;

        // 1. Obtener gastos (Expenses) utilizando los métodos de tu repositorio
        if (type == null || "expense".equals(type)) {
            List<Expense> expenses = (groupUuid != null)
                    ? expenseRepository.findByPaidByIdAndGroupId(userId, groupUuid)
                    : expenseRepository.findByPaidById(userId);

            list.addAll(expenses.stream().map(e -> TransactionHistoryDTO.builder()
                    .id(e.getId()) // Pasa directo como String
                    .type("EXPENSE")
                    .description(e.getDescription())
                    .groupName(e.getGroup() != null ? e.getGroup().getName() : "Sin Grupo")
                    .amount(e.getAmount() != null ? e.getAmount().doubleValue() : 0.0) // CORREGIDO: .doubleValue() para BigDecimal
                    .date(e.getCreatedAt())
                    .build()).collect(Collectors.toList()));
        }

        // 2. Obtener pagos (Deudas PAID) con mapeos y conversiones seguras
        if (type == null || "payment".equals(type)) {
            List<Debt> payments = (groupUuid != null)
                    ? debtRepository.findPaidByUserIdAndGroupId(userId, groupUuid)
                    : debtRepository.findPaidByUserId(userId);

            list.addAll(payments.stream().map(d -> TransactionHistoryDTO.builder()
                    .id(d.getId()) // CORREGIDO: Pasa directo como String, ya no requiere UUID.fromString
                    .type("PAYMENT")
                    .description("Pago de deuda: " + d.getExpenseId())
                    .groupName(d.getGroup() != null ? d.getGroup().getName() : "Sin Grupo")
                    .amount(d.getAmount() != null ? d.getAmount().doubleValue() : 0.0)
                    .date(d.getPaidAt())
                    .build()).collect(Collectors.toList()));
        }

        // 3. Ordenar por fecha descendente de forma segura (Previene caídas por nulos)
        list.sort((a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().compareTo(a.getDate());
        });

        return list;
    }
}