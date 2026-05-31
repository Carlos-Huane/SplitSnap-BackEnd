package com.splitsnap.dto.group;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class CreateGroupRequest {

    @Schema(example = "Departamento 4B")
    @NotBlank(message = "El nombre del grupo es obligatorio")
    @Size(max = 100, message = "El nombre no puede superar 100 caracteres")
    private String name;

    @Schema(example = "🏠")
    private String emoji;

    @Schema(description = "IDs de usuarios a agregar al grupo (el creador se agrega automáticamente)")
    private List<UUID> memberIds = new ArrayList<>();
}
