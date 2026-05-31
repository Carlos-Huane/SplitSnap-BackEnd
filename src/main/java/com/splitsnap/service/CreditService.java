package com.splitsnap.service;

import com.splitsnap.dto.credit.BuyCreditsRequest;
import com.splitsnap.dto.credit.TransactionResponse;
import com.splitsnap.model.CreditTransaction;
import com.splitsnap.model.User;
import com.splitsnap.repository.CreditTransactionRepository;
import com.splitsnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CreditService {

    private final UserRepository userRepository;
    private final CreditTransactionRepository creditTransactionRepository;

    @Transactional
    public BigDecimal buyCredits(User user, BuyCreditsRequest request) {
        BigDecimal amountToAdd = BigDecimal.valueOf(request.getAmount());

        // 1. Actualizar balance
        user.setCredits(user.getCredits().add(amountToAdd));
        userRepository.save(user);

        // 2. Registrar transacción
        CreditTransaction tx = new CreditTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUser(user);
        tx.setAmount(amountToAdd.doubleValue());
        tx.setType("PURCHASE");
        tx.setCreatedAt(LocalDateTime.now());
        creditTransactionRepository.save(tx);

        return user.getCredits();
    }

    @Transactional(readOnly = true)
    public Map<String, Object> getCreditInfo(User user) {
        Map<String, Object> result = new HashMap<>();
        result.put("balance", user.getCredits());

        List<TransactionResponse> history = creditTransactionRepository.findByUserIdOrderByCreatedAtDesc(user.getId())
                .stream()
                .map(tx -> TransactionResponse.builder()
                        .amount(tx.getAmount())
                        .type(tx.getType())
                        .debtId(tx.getDebtId())
                        .createdAt(tx.getCreatedAt())
                        .build())
                .collect(Collectors.toList());

        result.put("history", history);
        return result;
    }
}