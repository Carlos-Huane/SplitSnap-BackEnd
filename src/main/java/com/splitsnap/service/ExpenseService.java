package com.splitsnap.service;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.model.Expense;
import com.splitsnap.model.ExpenseSplit;
import com.splitsnap.model.Group;
import com.splitsnap.model.User;
import com.splitsnap.repository.ExpenseRepository;
import com.splitsnap.repository.ExpenseSplitRepository;
import com.splitsnap.repository.GroupMemberRepository; // Asegúrate de tener este repo o usar tu GroupService
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final GroupService groupService;
    private final UserService userService;

    @Transactional
    public ExpenseResponse createExpense(UUID groupId, CreateExpenseRequest request, User authenticatedUser) {
        
        // 1. Verificar si el grupo existe
        Group group = groupService.findById(groupId);

        // 2. Regla de Negocio: Validar que el usuario que registra pertenece al grupo
        boolean isMember = groupMemberRepository.existsByGroupIdAndUserId(groupId, authenticatedUser.getId());
        if (!isMember) {
            throw new BusinessException("No tienes autorización para registrar gastos en este grupo.");
        }

        // 3. Regla de Negocio: Validar que los montos asignados sumen exactamente el total del gasto
        double totalSplitsSum = request.getSplitBetween().stream()
                .mapToDouble(CreateExpenseRequest.SplitEntry::getAmount)
                .sum();
        
        // Usamos un delta pequeño por posibles decimales flotantes
        if (Math.abs(totalSplitsSum - request.getAmount()) > 0.01) {
            throw new BusinessException("La suma de los montos individuales (" + totalSplitsSum + 
                                        ") no coincide con el monto total del gasto (" + request.getAmount() + ").");
        }

        // 4. Mapear y guardar el Gasto Principal (Quien pagó es el usuario autenticado)
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .group(group)
                .paidBy(authenticatedUser)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 5. Mapear y guardar cada desglose de la lista en expense_splits
        for (CreateExpenseRequest.SplitEntry entry : request.getSplitBetween()) {
            User member = userService.findById(entry.getUserId()); // Busca al miembro asignado
            
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(member)
                    .amount(entry.getAmount())
                    .build();
            
            expenseSplitRepository.save(split);
        }

        return ExpenseResponse.from(savedExpense);
    }
}