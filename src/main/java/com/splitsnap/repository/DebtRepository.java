package com.splitsnap.repository;

import com.splitsnap.model.Debt;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DebtRepository extends JpaRepository<Debt, String> {

    // Busca todas las deudas de un grupo específico sin importar el estado
    List<Debt> findByGroupId(String groupId);

    // Busca las deudas de un grupo filtradas rigurosamente por su estado (PENDING o PAID)
    List<Debt> findByGroupIdAndStatus(String groupId, String status);
}