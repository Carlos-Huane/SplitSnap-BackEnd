package com.splitsnap.service;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.MarkAsPaidRequest;
import com.splitsnap.dto.debt.UserDebtInfoDTO;
import com.splitsnap.exception.ResourceNotFoundException;
import com.splitsnap.model.CreditTransaction;
import com.splitsnap.model.Debt;
import com.splitsnap.model.User;
import com.splitsnap.repository.CreditTransactionRepository;
import com.splitsnap.repository.DebtRepository;
import com.splitsnap.repository.GroupMemberRepository;
import com.splitsnap.repository.GroupRepository;
import com.splitsnap.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DebtService {

    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;
    private final GroupMemberRepository groupMemberRepository;
    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public List<DebtResponse> getDebtsByGroup(UUID groupId, String status, User currentUser) {
        if (!groupRepository.existsById(groupId)) {
            throw new ResourceNotFoundException("El grupo con ID '" + groupId + "' no existe.");
        }
        assertIsMember(groupId, currentUser.getId());

        List<Debt> debts = (status != null && !status.trim().isEmpty())
                ? debtRepository.findByGroupIdAndStatus(groupId, status.toUpperCase())
                : debtRepository.findByGroupId(groupId);

        return debts.stream()
                .map(this::convertToDebtResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DebtResponse markDebtAsPaid(UUID groupId, String debtId, MarkAsPaidRequest request, User currentUser) {
        Debt debt = debtRepository.findByIdAndGroupId(debtId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada en este grupo"));

        if (!debt.getFromUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el deudor puede marcar esta deuda como pagada");
        }
        if ("PAID".equals(debt.getStatus())) {
            throw new IllegalStateException("Esta deuda ya ha sido pagada");
        }

        debt.setStatus("PAID");
        debt.setPaidAt(LocalDateTime.now());
        debt.setPaidWith(request.getPaidWith());

        debtRepository.save(debt);
        return convertToDebtResponse(debt);
    }

    @Transactional
    public DebtResponse payDebtWithCredits(UUID groupId, String debtId, User currentUser) {
        Debt debt = debtRepository.findByIdAndGroupId(debtId, groupId)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));

        if (!debt.getFromUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el deudor puede pagar esta deuda");
        }
        if ("PAID".equals(debt.getStatus())) {
            throw new IllegalStateException("Esta deuda ya ha sido pagada");
        }

        BigDecimal debtAmount = debt.getAmount();
        if (currentUser.getCredits().compareTo(debtAmount) < 0) {
            throw new IllegalArgumentException("Créditos insuficientes");
        }

        currentUser.setCredits(currentUser.getCredits().subtract(debtAmount));
        userRepository.save(currentUser);

        debt.setStatus("PAID");
        debt.setPaidWith("credits");
        debt.setPaidAt(LocalDateTime.now());
        debtRepository.save(debt);

        CreditTransaction tx = new CreditTransaction();
        tx.setId(UUID.randomUUID().toString());
        tx.setUser(currentUser);
        tx.setAmount(debtAmount.doubleValue());
        tx.setType("SPEND");
        tx.setDebtId(debt.getId());
        tx.setCreatedAt(LocalDateTime.now());
        creditTransactionRepository.save(tx);

        return convertToDebtResponse(debt);
    }

    private void assertIsMember(UUID groupId, UUID userId) {
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new AccessDeniedException("No eres miembro de este grupo.");
        }
    }

    private DebtResponse convertToDebtResponse(Debt debt) {
        DebtResponse response = new DebtResponse();
        response.setId(debt.getId());
        response.setAmount(debt.getAmount());
        response.setStatus(debt.getStatus());
        response.setExpenseId(debt.getExpenseId());
        response.setPaidAt(debt.getPaidAt());
        response.setPaidWith(debt.getPaidWith());

        User from = debt.getFromUser();
        if (from != null) {
            response.setFromUser(new UserDebtInfoDTO(from.getId(), from.getName(), from.getEmail(), from.getAvatarUrl()));
        }

        User to = debt.getToUser();
        if (to != null) {
            response.setToUser(new UserDebtInfoDTO(to.getId(), to.getName(), to.getEmail(), to.getAvatarUrl()));
        }

        return response;
    }
}
