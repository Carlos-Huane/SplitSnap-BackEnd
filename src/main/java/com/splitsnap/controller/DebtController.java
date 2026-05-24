package com.splitsnap.controller;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.MarkAsPaidRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/groups")
public class DebtController {

    private final DebtService debtService;

    // Inyección de dependencias por constructor
    public DebtController(DebtService debtService) {
        this.debtService = debtService;
    }

    /**
     * HU-5.1: Ver deudas de un grupo
     * Endpoint: GET /api/groups/{groupId}/debts?status={status}
     */
    @GetMapping("/{groupId}/debts")
    public ResponseEntity<?> getDebtsByGroup(
            @PathVariable String groupId,
            @RequestParam(required = false) String status) {

        try {
            List<DebtResponse> debts = debtService.getDebtsByGroup(groupId, status);
            return ResponseEntity.ok(debts);
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error interno: " + e.getMessage());
        }
    }

    @PutMapping("/{groupId}/debts/{debtId}/mark-paid")
    public ResponseEntity<?> markAsPaid(
            @PathVariable String groupId,
            @PathVariable String debtId,
            @Valid @RequestBody MarkAsPaidRequest request,
            @AuthenticationPrincipal User currentUser) {

        try {
            return ResponseEntity.ok(debtService.markDebtAsPaid(groupId, debtId, request, currentUser));
        } catch (Exception e) {
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }

    @PutMapping("/{groupId}/debts/{debtId}/pay-credits")
    public ResponseEntity<?> payWithCredits(
            @PathVariable String groupId,
            @PathVariable String debtId,
            @AuthenticationPrincipal User currentUser) {

        try {
            // Llamada al servicio
            DebtResponse response = debtService.payDebtWithCredits(groupId, debtId, currentUser);
            return ResponseEntity.ok(response);

        } catch (IllegalArgumentException e) {
            // Este catch maneja específicamente los "Créditos insuficientes"
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (IllegalStateException e) {
            // Este catch maneja el estado "Ya pagada"
            return ResponseEntity.badRequest().body(e.getMessage());

        } catch (AccessDeniedException e) {
            // Maneja intentos de pago de deudas ajenas
            return ResponseEntity.status(403).body(e.getMessage());

        } catch (Exception e) {
            // Manejo de errores inesperados (Logueo obligatorio)
            e.printStackTrace();
            return ResponseEntity.internalServerError().body("Ocurrió un error al procesar el pago con créditos.");
        }
    }
}