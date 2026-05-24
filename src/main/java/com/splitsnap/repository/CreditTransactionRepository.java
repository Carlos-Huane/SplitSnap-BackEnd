package com.splitsnap.repository;

import com.splitsnap.model.CreditTransaction;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.UUID;

@Repository
public interface CreditTransactionRepository extends JpaRepository<CreditTransaction, String> {
    // Si en tu modelo el campo se llama 'user', Spring lo interpreta como 'user_id'
    List<CreditTransaction> findByUserIdOrderByCreatedAtDesc(UUID userId);
}