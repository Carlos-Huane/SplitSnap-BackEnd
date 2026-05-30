package com.splitsnap.controller;

import com.splitsnap.model.User;
import com.splitsnap.service.TransactionService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users/me/transactions")
@RequiredArgsConstructor
/**
 * @apiDefine TransactionsGroup Historial consolidado de movimientos.
 */
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    /**
     * @api {get} /api/users/me/transactions Historial completo
     * @apiName GetFullHistory
     * @apiGroup Transactions
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} [groupId] Filtra por grupo.
     * @apiParam {String} [type] Filtra por tipo.
     */
    public ResponseEntity<?> getFullHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String type) {

        return ResponseEntity.ok(transactionService.getHistory(user, groupId, type));
    }
}
