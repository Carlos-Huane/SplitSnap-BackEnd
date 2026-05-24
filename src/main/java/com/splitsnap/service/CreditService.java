package com.splitsnap.service;

import com.splitsnap.dto.credit.BuyCreditsRequest;
import com.splitsnap.model.CreditTransaction;
import com.splitsnap.model.User;
import com.splitsnap.repository.CreditTransactionRepository;
import com.splitsnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

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
}