package com.splitsnap.controller;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.MarkAsPaidRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.DebtService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
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
            // ESTA LÍNEA ES LA MÁS IMPORTANTE
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
            e.printStackTrace(); // <--- Esto obligará a que el error aparezca en consola
            return ResponseEntity.internalServerError().body("Error: " + e.getMessage());
        }
    }
}