package com.splitsnap.service;

import com.splitsnap.dto.transaction.TransactionHistoryDTO;
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

        // 1. Obtener gastos (Expenses)
        if (type == null || "expense".equals(type)) {
            var expenses = (groupUuid != null)
                    ? expenseRepository.findByCreatedByIdAndGroupId(userId, groupUuid)
                    : expenseRepository.findByCreatedById(userId);

            list.addAll(expenses.stream().map(e -> TransactionHistoryDTO.builder()
                    .id(e.getId())
                    .type("EXPENSE")
                    .description(e.getDescription())
                    .groupName(e.getGroup().getName())
                    .amount(e.getAmount().doubleValue())
                    .date(e.getCreatedAt())
                    .build()).collect(Collectors.toList()));
        }

        // 2. Obtener pagos (Deudas PAID)
        if (type == null || "payment".equals(type)) {
            var payments = (groupUuid != null)
                    ? debtRepository.findPaidByUserIdAndGroupId(userId, groupUuid)
                    : debtRepository.findPaidByUserId(userId);

            list.addAll(payments.stream().map(d -> TransactionHistoryDTO.builder()
                    .id(d.getId())
                    .type("PAYMENT")
                    .description("Pago de deuda: " + d.getExpenseId())
                    .groupName(d.getGroup().getName())
                    .amount(d.getAmount().doubleValue())
                    .date(d.getPaidAt())
                    .build()).collect(Collectors.toList()));
        }

        // 3. Ordenar por fecha descendente
        list.sort((a, b) -> b.getDate().compareTo(a.getDate()));

        return list;
    }
}