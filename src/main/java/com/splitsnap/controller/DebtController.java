package com.splitsnap.controller;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.MarkAsPaidRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.DebtService;
import io.swagger.v3.oas.annotations.Operation;
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
    @Operation(summary = "Ver deudas de un grupo (HU-5.1)")
    public ResponseEntity<List<DebtResponse>> getDebtsByGroup(
            @PathVariable UUID groupId,
            @RequestParam(required = false) String status,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.getDebtsByGroup(groupId, status, currentUser));
    }

    @PutMapping("/{groupId}/debts/{debtId}/mark-paid")
    @Operation(summary = "Marcar deuda como pagada (HU-5.2)")
    public ResponseEntity<DebtResponse> markAsPaid(
            @PathVariable UUID groupId,
            @PathVariable String debtId,
            @Valid @RequestBody MarkAsPaidRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.markDebtAsPaid(groupId, debtId, request, currentUser));
    }

    @PutMapping("/{groupId}/debts/{debtId}/pay-credits")
    @Operation(summary = "Pagar deuda con créditos del sistema (HU-5.3)")
    public ResponseEntity<DebtResponse> payWithCredits(
            @PathVariable UUID groupId,
            @PathVariable String debtId,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(debtService.payDebtWithCredits(groupId, debtId, currentUser));
    }
}
