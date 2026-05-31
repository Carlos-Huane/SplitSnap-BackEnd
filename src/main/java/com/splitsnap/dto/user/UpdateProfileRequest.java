package com.splitsnap.dto.user;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {

    @Schema(example = "Carlos Huane")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String name;

    @Schema(example = "carlos@splitsnap.com")
    @Email(message = "Formato de email inválido")
    private String email;

    @Schema(example = "987654321")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido (7-15 dígitos)")
    private String phone;

    @Schema(example = "123456")
    private String currentPassword;

    @Schema(example = "nueva123")
    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String newPassword;
}
