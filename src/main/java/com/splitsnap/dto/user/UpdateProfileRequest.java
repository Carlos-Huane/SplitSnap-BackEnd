package com.splitsnap.dto.user;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class UpdateProfileRequest {

    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    private String name;

    @Email(message = "Formato de email inválido")
    private String email;

    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido (7-15 dígitos)")
    private String phone;

    private String currentPassword;

    @Size(min = 6, message = "La nueva contraseña debe tener al menos 6 caracteres")
    private String newPassword;
}
