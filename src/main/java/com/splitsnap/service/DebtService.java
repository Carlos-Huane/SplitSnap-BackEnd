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
import com.splitsnap.repository.GroupRepository;
import com.splitsnap.repository.UserRepository;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;

    private final CreditTransactionRepository creditTransactionRepository;
    private final UserRepository userRepository;

    // Actualiza el constructor para incluirlos:
    public DebtService(DebtRepository debtRepository,
                       GroupRepository groupRepository,
                       CreditTransactionRepository creditTransactionRepository,
                       UserRepository userRepository) {
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
        this.creditTransactionRepository = creditTransactionRepository;
        this.userRepository = userRepository;
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getDebtsByGroup(String groupId, String status) {
        // 1. Validar formato UUID
        UUID groupUuid;
        try {
            groupUuid = UUID.fromString(groupId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("El formato del ID de grupo es inválido.");
        }

        // 2. Verificar existencia usando el UUID
        if (!groupRepository.existsById(groupUuid)) {
            throw new ResourceNotFoundException("El grupo con ID '" + groupId + "' no existe.");
        }

        List<Debt> debts;
        if (status != null && !status.trim().isEmpty()) {
            debts = debtRepository.findByGroupIdAndStatus(groupUuid, status.toUpperCase()); // Pasamos UUID
        } else {
            debts = debtRepository.findByGroupId(groupUuid); // Pasamos UUID
        }

        return debts.stream()
                .map(this::convertToDebtResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public DebtResponse markDebtAsPaid(String groupId, String debtId, MarkAsPaidRequest request, User currentUser) {
        // 1. Convertir el groupId de String a UUID
        UUID groupUuid;
        try {
            groupUuid = UUID.fromString(groupId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("El formato del ID de grupo es inválido.");
        }

        // 2. Buscar la deuda usando el groupUuid convertido
        Debt debt = debtRepository.findByIdAndGroupId(debtId, groupUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada en este grupo"));

        // 3. Validación de seguridad
        if (!debt.getFromUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el deudor puede marcar esta deuda como pagada");
        }

        // 4. Actualizar estado
        debt.setStatus("PAID");
        debt.setPaidAt(LocalDateTime.now());
        debt.setPaidWith(request.getPaidWith());

        debtRepository.save(debt);
        return convertToDebtResponse(debt);
    }

    @Transactional
    public DebtResponse payDebtWithCredits(String groupId, String debtId, User currentUser) {
        UUID groupUuid;
        try {
            groupUuid = UUID.fromString(groupId);
        } catch (IllegalArgumentException e) {
            throw new ResourceNotFoundException("Formato de ID de grupo inválido");
        }
        Debt debt = debtRepository.findByIdAndGroupId(debtId, groupUuid)
                .orElseThrow(() -> new ResourceNotFoundException("Deuda no encontrada"));

        // 2. Validaciones de negocio
        if (!debt.getFromUser().getId().equals(currentUser.getId())) {
            throw new AccessDeniedException("Solo el deudor puede pagar esta deuda");
        }
        if ("PAID".equals(debt.getStatus())) {
            throw new IllegalStateException("Esta deuda ya ha sido pagada");
        }

        // 3. Convertir monto de deuda (asumiendo que es Double, lo convertimos a BigDecimal)
        BigDecimal debtAmount = debt.getAmount();

        // 4. Validar saldo (compareTo devuelve -1 si currentUser.getCredits() < debtAmount)
        if (currentUser.getCredits().compareTo(debtAmount) < 0) {
            throw new IllegalArgumentException("Créditos insuficientes");
        }

        // 5. Aplicar descuento
        currentUser.setCredits(currentUser.getCredits().subtract(debtAmount));
        userRepository.save(currentUser);

        // 6. Actualizar Deuda
        debt.setStatus("PAID");
        debt.setPaidWith("credits");
        debt.setPaidAt(LocalDateTime.now());
        debtRepository.save(debt);

        // 7. Registrar transacción
        CreditTransaction tx = new CreditTransaction();
        tx.setId(UUID.randomUUID().toString()); // UUID manual
        tx.setUser(currentUser);
        tx.setAmount(debtAmount.doubleValue());
        tx.setType("SPEND");
        tx.setDebtId(debt.getId());
        tx.setCreatedAt(LocalDateTime.now());
        creditTransactionRepository.save(tx);

        return convertToDebtResponse(debt);
    }

    // Método helper privado para transformar la entidad al DTO exacto requerido
    private DebtResponse convertToDebtResponse(Debt debt) {
        DebtResponse response = new DebtResponse();
        response.setId(debt.getId());
        response.setAmount(debt.getAmount());
        response.setStatus(debt.getStatus());
        response.setExpenseId(debt.getExpenseId());
        response.setPaidAt(debt.getPaidAt());
        response.setPaidWith(debt.getPaidWith());

        // Mapear la información reducida del deudor (fromUser)
        User from = debt.getFromUser();
        if (from != null) {
            response.setFromUser(new UserDebtInfoDTO(from.getId(), from.getName(), from.getEmail(), from.getAvatarUrl()));
        }

        // Mapear la información reducida del acreedor (toUser)
        User to = debt.getToUser();
        if (to != null) {
            response.setToUser(new UserDebtInfoDTO(to.getId(), to.getName(), to.getEmail(), to.getAvatarUrl()));
        }

        return response;
    }
}