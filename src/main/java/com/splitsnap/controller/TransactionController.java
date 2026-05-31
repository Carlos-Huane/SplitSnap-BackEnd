package com.splitsnap.controller;

import com.splitsnap.dto.transaction.TransactionHistoryDTO;
import com.splitsnap.model.User;
import com.splitsnap.service.TransactionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/users/me/transactions")
@RequiredArgsConstructor
@Tag(name = "Transactions", description = "Historial consolidado de movimientos del usuario")
@SecurityRequirement(name = "bearerAuth")
/**
 * @apiDefine TransactionsGroup Historial consolidado de movimientos.
 */
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    @Operation(
        summary = "Historial consolidado del usuario (HU-5.6)",
        description = "Combina gastos creados y deudas pagadas del usuario. Filtros opcionales por grupo y tipo (expense | payment)."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Historial obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {get} /api/users/me/transactions Historial completo
     * @apiName GetFullHistory
     * @apiGroup Transactions
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} [groupId] Filtra por grupo.
     * @apiParam {String} [type] Filtra por tipo.
     */
    public ResponseEntity<List<TransactionHistoryDTO>> getFullHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String type) {

        return ResponseEntity.ok(transactionService.getHistory(user, groupId, type));
    }
}
