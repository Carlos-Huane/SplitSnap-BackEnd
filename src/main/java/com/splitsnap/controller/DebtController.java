package com.splitsnap.controller;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.MarkAsPaidRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
@Tag(name = "Debts", description = "Consulta y pago de deudas")
@SecurityRequirement(name = "bearerAuth")
public class DebtController {

    private final DebtService debtService;

    @GetMapping("/{groupId}/debts")
    @Operation(
        summary = "Ver deudas de un grupo (HU-5.1)",
        description = "Lista las deudas del grupo. Filtro opcional por estado: PENDING o PAID. Requiere ser miembro."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Listado obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "No eres miembro del grupo"),
        @ApiResponse(responseCode = "404", description = "Grupo no encontrado")
    })
    public ResponseEntity<List<DebtResponse>> getDebtsByGroup(
            @PathVariable UUID groupId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.getDebtsByGroup(groupId, status, currentUser));
    }

    @PutMapping("/{groupId}/debts/{debtId}/mark-paid")
    @Operation(
        summary = "Marcar deuda como pagada (HU-5.2)",
        description = "El deudor marca su deuda como saldada indicando el método (yape, paypal, efectivo)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deuda marcada como pagada"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Solo el deudor puede marcar la deuda"),
        @ApiResponse(responseCode = "404", description = "Deuda no encontrada en el grupo"),
        @ApiResponse(responseCode = "409", description = "La deuda ya estaba pagada")
    })
    public ResponseEntity<DebtResponse> markAsPaid(
            @PathVariable UUID groupId,
            @PathVariable String debtId,
            @Valid @RequestBody MarkAsPaidRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.markDebtAsPaid(groupId, debtId, request, currentUser));
    }

    @PutMapping("/{groupId}/debts/{debtId}/pay-credits")
    @Operation(
        summary = "Pagar deuda con créditos del sistema (HU-5.3)",
        description = "Descuenta créditos del usuario para saldar la deuda. Registra la transacción tipo SPEND."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Deuda pagada con créditos"),
        @ApiResponse(responseCode = "400", description = "Créditos insuficientes"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "403", description = "Solo el deudor puede pagar la deuda"),
        @ApiResponse(responseCode = "404", description = "Deuda no encontrada en el grupo"),
        @ApiResponse(responseCode = "409", description = "La deuda ya estaba pagada")
    })
    public ResponseEntity<DebtResponse> payWithCredits(
            @PathVariable UUID groupId,
            @PathVariable String debtId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.payDebtWithCredits(groupId, debtId, currentUser));
    }
}
