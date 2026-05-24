package com.splitsnap.controller;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseDetailResponse;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.model.User;
import com.splitsnap.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // <-- IMPORTANTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List; // <-- ¡ESTA ES LA QUE FALTABA PARA EL GET!
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gestión de gastos de SplitSnap")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/{groupId}/expenses")
    @Operation(summary = "Registrar un gasto manual en un grupo (SCRUM-96)")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal User authenticatedUser) {
        
        ExpenseResponse response = expenseService.createExpense(groupId, request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/expenses")
    @Operation(summary = "Listar todos los gastos de un grupo ordenados por fecha (SCRUM-98)")
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroup(@PathVariable UUID groupId) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{expenseId}")
    @Operation(summary = "Ver el detalle completo de un gasto y sus desgloses (SCRUM-99)")
    public ResponseEntity<ExpenseDetailResponse> getExpenseDetails(@PathVariable UUID expenseId) {
        ExpenseDetailResponse detail = expenseService.getExpenseDetails(expenseId);
        return ResponseEntity.ok(detail);
    }    
}