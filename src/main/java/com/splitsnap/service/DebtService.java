package com.splitsnap.service;

import com.splitsnap.dto.debt.DebtResponse;
import com.splitsnap.dto.debt.UserDebtInfoDTO;
import com.splitsnap.exception.ResourceNotFoundException;
import com.splitsnap.model.Debt;
import com.splitsnap.model.User;
import com.splitsnap.repository.DebtRepository;
import com.splitsnap.repository.GroupRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class DebtService {

    private final DebtRepository debtRepository;
    private final GroupRepository groupRepository;

    public DebtService(DebtRepository debtRepository, GroupRepository groupRepository) {
        this.debtRepository = debtRepository;
        this.groupRepository = groupRepository;
    }

    @Transactional(readOnly = true)
    public List<DebtResponse> getDebtsByGroup(String groupId, String status) {

        // 1. Convertir el String a UUID para que el repositorio no falle
        UUID groupUuid = UUID.fromString(groupId);

        // 2. Validar usando el UUID convertido
        if (!groupRepository.existsById(groupUuid)) {
            throw new ResourceNotFoundException("El grupo con ID '" + groupId + "' no existe.");
        }

        List<Debt> debts;

        // 2. Buscar las deudas aplicando el filtro opcional de estado si viene informado
        if (status != null && !status.trim().isEmpty()) {
            debts = debtRepository.findByGroupIdAndStatus(groupId, status.toUpperCase());
        } else {
            debts = debtRepository.findByGroupId(groupId);
        }

        // 3. Transformar la lista de entidades a DTOs de respuesta
        return debts.stream()
                .map(this::convertToDebtResponse)
                .collect(Collectors.toList());
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