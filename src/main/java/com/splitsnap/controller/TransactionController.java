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
public class TransactionController {

    private final TransactionService transactionService;

    @GetMapping
    public ResponseEntity<?> getFullHistory(
            @AuthenticationPrincipal User user,
            @RequestParam(required = false) String groupId,
            @RequestParam(required = false) String type) {

        return ResponseEntity.ok(transactionService.getHistory(user, groupId, type));
    }
}
