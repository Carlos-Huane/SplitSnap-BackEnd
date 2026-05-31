package com.splitsnap.controller;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseDetailResponse;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.dto.expense.OcrResponse;
import com.splitsnap.model.User;

import com.splitsnap.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus; // <-- IMPORTANTE
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import org.springframework.http.MediaType;
import java.util.List; // <-- ¡ESTA ES LA QUE FALTABA PARA EL GET!
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gestión de gastos de SplitSnap")
@SecurityRequirement(name = "bearerAuth")
/**
 * @apiDefine ExpensesGroup Gastos, detalle y OCR.
 */
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
        summary = "Listar todos los gastos de un grupo ordenados por fecha (SCRUM-98)",
        description = "Obtiene todos los gastos registrados en un grupo específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Lista obtenida correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    /**
     * @api {get} /api/groups/:groupId/expenses Listar gastos del grupo
     * @apiName GetExpensesByGroup
     * @apiGroup Expenses
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} groupId ID del grupo.
     */
    public ResponseEntity<List<ExpenseResponse>> getExpensesByGroup(@PathVariable UUID groupId) {
        List<ExpenseResponse> expenses = expenseService.getExpensesByGroup(groupId);
        return ResponseEntity.ok(expenses);
    }

    @GetMapping("/expenses/{expenseId}")
    @Operation(
        summary = "Ver el detalle completo de un gasto y sus desgloses (SCRUM-99)",
        description = "Obtiene toda la información asociada a un gasto específico"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Detalle obtenido correctamente"),
        @ApiResponse(responseCode = "404", description = "Gasto no encontrado"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {get} /api/groups/expenses/:expenseId Detalle del gasto
     * @apiName GetExpenseDetails
     * @apiGroup Expenses
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} expenseId ID del gasto.
     */
    public ResponseEntity<ExpenseDetailResponse> getExpenseDetails(@PathVariable UUID expenseId) {
        ExpenseDetailResponse detail = expenseService.getExpenseDetails(expenseId);
        return ResponseEntity.ok(detail);
    }  
    
    @PostMapping(value = "/expenses/ocr", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @Operation(
        summary = "Escanear y extraer datos de un recibo mediante OCR (SCRUM-100)",
        description = "Procesa una imagen o PDF de recibo y extrae automáticamente los datos detectados"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Recibo procesado correctamente"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {post} /api/groups/expenses/ocr Escanear recibo
     * @apiName UploadReceiptOcr
     * @apiGroup Expenses
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {File} file Imagen o PDF del recibo.
     */
    public ResponseEntity<OcrResponse> uploadReceiptOcr(@RequestParam("file") MultipartFile file) {
        OcrResponse response = expenseService.processReceiptOcr(file);
        return ResponseEntity.ok(response);
    }    
}