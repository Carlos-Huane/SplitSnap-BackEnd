package com.splitsnap.controller;

import com.splitsnap.dto.credit.BuyCreditsRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.CreditService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/credits")
@RequiredArgsConstructor
public class CreditController {

    private final CreditService creditService;

    @PostMapping("/buy")
    public ResponseEntity<?> buyCredits(
            @Valid @RequestBody BuyCreditsRequest request,
            @AuthenticationPrincipal User currentUser) {

        BigDecimal newBalance = creditService.buyCredits(currentUser, request);
        return ResponseEntity.ok(Map.of("message", "Compra exitosa", "newBalance", newBalance));
    }

    @GetMapping("/me")
    public ResponseEntity<?> getCreditInfo(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(creditService.getCreditInfo(currentUser));
    }
}