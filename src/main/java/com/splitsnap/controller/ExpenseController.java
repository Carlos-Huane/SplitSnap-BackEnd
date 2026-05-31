package com.splitsnap.controller;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseDetailResponse;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.model.User;
import com.splitsnap.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gestión de gastos de SplitSnap")
@SecurityRequirement(name = "bearerAuth")
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping("/{groupId}/expenses")
    @Operation(
        summary = "Registrar un gasto manual en un grupo (SCRUM-96)",
        description = "Registra un nuevo gasto dentro de un grupo y distribuye la deuda entre los participantes"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Gasto registrado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    /**
     * @api {post} /api/groups/:groupId/expenses Registrar gasto
     * @apiName CreateExpense
     * @apiGroup Expenses
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} groupId ID del grupo.
     * @apiParam {String} description Descripción del gasto.
     * @apiParam {Number} amount Monto total.
     */
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request,
            @AuthenticationPrincipal User authenticatedUser) {

        ExpenseResponse response = expenseService.createExpense(groupId, request, authenticatedUser);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @GetMapping("/{groupId}/expenses")
    @Operation(
        summary = "Listar todos los gastos de un grupo ordenados por fecha (HU-4.3)",
        description = "Devuelve todos los gastos del grupo ordenados por fecha descendente. Requiere ser miembro."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No eres miembro del grupo"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroup(
            @PathVariable UUID groupId,
            @AuthenticationPrincipal User authenticatedUser) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId, authenticatedUser);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/{groupId}/expenses/{expenseId}")
    @Operation(
        summary = "Ver el detalle completo de un gasto y sus desgloses (HU-4.4)",
        description = "Devuelve los splits por usuario del gasto. Requiere ser miembro del grupo."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No eres miembro del grupo"),
        @ApiResponse(responseCode = "404", description = "Grupo o gasto no encontrado")
    })
    public ResponseEntity<ExpenseDetailResponse> getExpenseDetails(
            @PathVariable UUID groupId,
            @PathVariable UUID expenseId,
            @AuthenticationPrincipal User authenticatedUser) {
        ExpenseDetailResponse detail = expenseService.getExpenseDetails(groupId, expenseId, authenticatedUser);
        return ResponseEntity.ok(detail);
    }
}
