package com.splitsnap.service;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.model.Expense;
import com.splitsnap.model.ExpenseSplit;
import com.splitsnap.model.Debt; // IMPORTANTE: Agrega la entidad Debt
import com.splitsnap.model.Group;
import com.splitsnap.model.User;
import com.splitsnap.repository.ExpenseRepository;
import com.splitsnap.repository.ExpenseSplitRepository;
import com.splitsnap.repository.GroupMemberRepository;
import com.splitsnap.repository.DebtRepository; // IMPORTANTE: Agrega el repositorio de Deudas
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final ExpenseSplitRepository expenseSplitRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final DebtRepository debtRepository; // INYECTADO para SCRUM-97
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
        
        if (Math.abs(totalSplitsSum - request.getAmount()) > 0.01) {
            throw new BusinessException("La suma de los montos individuales (" + totalSplitsSum + 
                    ") no coincide con el monto total del gasto (" + request.getAmount() + ").");
        }

        // 4. Mapear y guardar el Gasto Principal
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .group(group)
                .paidBy(authenticatedUser)
                .build();

        Expense savedExpense = expenseRepository.save(expense);

        // 5. Mapear, guardar splits y GENERAR DEUDAS (SCRUM-97)
        for (CreateExpenseRequest.SplitEntry entry : request.getSplitBetween()) {
            User member = userService.findById(entry.getUserId());
            
            // A. Guardamos el split tradicional
            ExpenseSplit split = ExpenseSplit.builder()
                    .expense(savedExpense)
                    .user(member)
                    .amount(entry.getAmount())
                    .build();
            
            expenseSplitRepository.save(split);

            // B. LÓGICA SCRUM-97: Si el usuario del split no es quien pagó, le creamos una deuda automática
            if (!member.getId().equals(authenticatedUser.getId())) {
                Debt debt = new Debt();
                
                // Como tu entidad Debt maneja el ID como String (largo 36), generamos un UUID en texto
                debt.setId(UUID.randomUUID().toString()); 
                debt.setGroup(group);
                debt.setExpenseId(savedExpense.getId().toString()); // Vinculamos la deuda a este gasto
                debt.setFromUser(member);                            // El que debe (miembro del split)
                debt.setToUser(authenticatedUser);                   // A quien le debe (el que pagó)
                debt.setAmount(BigDecimal.valueOf(entry.getAmount())); // El monto asignado a ese miembro
                debt.setStatus("PENDING");                           // Estado inicial por defecto

                debtRepository.save(debt);
            }
        }

        return ExpenseResponse.from(savedExpense);
    }
}