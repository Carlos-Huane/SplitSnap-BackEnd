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

        // 1. Obtener gastos (Expenses)
        if (type == null || "expense".equals(type)) {
            List<Expense> expenses = (groupUuid != null)
                    ? expenseRepository.findByPaidByIdAndGroupId(userId, groupUuid)
                    : expenseRepository.findByPaidById(userId);

            list.addAll(expenses.stream().map(e -> new TransactionHistoryDTO(
                    e.getId(),
                    "EXPENSE",
                    e.getDescription(),
                    e.getGroup() != null ? e.getGroup().getName() : "Sin Grupo",
                    e.getAmount(), 
                    e.getCreatedAt()
            )).collect(Collectors.toList()));
        }

        // 2. Obtener pagos (Deudas PAID)
        if (type == null || "payment".equals(type)) {
            List<Debt> payments = (groupUuid != null)
                    ? debtRepository.findPaidByUserIdAndGroupId(userId, groupUuid)
                    : debtRepository.findPaidByUserId(userId);

            list.addAll(payments.stream().map(d -> new TransactionHistoryDTO(
                    d.getId() != null ? UUID.fromString(d.getId()) : null, // CORREGIDO: Conversión de String a UUID
                    "PAYMENT",
                    "Pago de deudas en grupo",
                    d.getGroup() != null ? d.getGroup().getName() : "Sin Grupo",
                    d.getAmount() != null ? d.getAmount().doubleValue() : 0.0, // Conversión segura a Double
                    d.getPaidAt() // Es LocalDateTime, pasa directo
            )).collect(Collectors.toList()));
        }

        // 3. Ordenar por fecha descendente
        list.sort((a, b) -> {
            if (a.getDate() == null || b.getDate() == null) return 0;
            return b.getDate().compareTo(a.getDate());
        });

        return list;
    }
}