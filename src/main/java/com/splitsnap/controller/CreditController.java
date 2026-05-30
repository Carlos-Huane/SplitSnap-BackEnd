package com.splitsnap.controller;

import com.splitsnap.dto.credit.BuyCreditsRequest;
import com.splitsnap.model.User;
import com.splitsnap.service.CreditService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.Map;

@RestController
@RequestMapping("/api/users/me/credits")
@RequiredArgsConstructor
@Tag(name = "Credits", description = "Gestión de créditos del usuario autenticado")
@SecurityRequirement(name = "bearerAuth")
public class CreditController {

    private final CreditService creditService;

    @GetMapping
    @Operation(summary = "Ver balance e historial de créditos (HU-5.4)")
    public ResponseEntity<Map<String, Object>> getCreditInfo(@AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(creditService.getCreditInfo(currentUser));
    }

    @PostMapping("/buy")
    @Operation(summary = "Comprar créditos (HU-5.5)")
    public ResponseEntity<Map<String, Object>> buyCredits(
            @Valid @RequestBody BuyCreditsRequest request,
            @AuthenticationPrincipal User currentUser) {

        BigDecimal newBalance = creditService.buyCredits(currentUser, request);
        return ResponseEntity.ok(Map.of("message", "Compra exitosa", "newBalance", newBalance));
    }
}
