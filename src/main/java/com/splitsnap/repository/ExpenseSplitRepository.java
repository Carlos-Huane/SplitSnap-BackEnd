package com.splitsnap.repository;

import com.splitsnap.model.ExpenseSplit;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface ExpenseSplitRepository extends JpaRepository<ExpenseSplit, UUID> {
    // Aquí puedes agregar métodos personalizados más adelante si los necesitas
}