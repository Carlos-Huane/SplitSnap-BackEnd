package com.splitsnap.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.util.UUID;

@Getter @Setter
public class AddMemberRequest {

    @Schema(description = "ID del usuario a agregar al grupo")
    @NotNull(message = "El userId es obligatorio")
    private UUID userId;
}
