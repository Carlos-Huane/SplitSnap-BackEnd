package com.splitsnap.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class CreateExpenseRequest {

    @Schema(example = "Pizza familiar y gaseosas")
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @Schema(example = "85.50")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double amount;

    @Schema(description = "ID del usuario que pagó el gasto. Si es null, se asume el usuario autenticado.")
    private UUID paidBy;

    @Schema(description = "Fecha del gasto. Si es null, se usa la fecha actual.", example = "2026-05-30")
    private LocalDate date;

    @Schema(description = "Lista detallada de cómo se divide el gasto entre los miembros")
    @NotEmpty(message = "Debe incluir al menos una división de gasto")
    @Valid
    private List<SplitEntry> splitBetween;

    @Getter @Setter
    public static class SplitEntry {

        @NotNull(message = "El ID del usuario es obligatorio")
        private UUID userId;

        @NotNull(message = "El monto por usuario es obligatorio")
        @PositiveOrZero(message = "El monto por usuario no puede ser negativo")
        private Double amount;
    }
}
