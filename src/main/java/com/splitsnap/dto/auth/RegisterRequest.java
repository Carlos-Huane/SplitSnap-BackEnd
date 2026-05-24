package com.splitsnap.dto.auth;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class RegisterRequest {

    @Schema(example = "Carlos Huane")
    @NotBlank(message = "El nombre es obligatorio")
    @Size(min = 3, message = "El nombre debe tener al menos 3 caracteres")
    @Pattern(regexp = "^[a-zA-ZáéíóúÁÉÍÓÚñÑ]+(\\s[a-zA-ZáéíóúÁÉÍÓÚñÑ]+)+$",
             message = "Ingresa nombre y apellido, solo letras")
    private String name;

    @Schema(example = "carlos@splitsnap.com")
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Formato de email inválido")
    private String email;

    @Schema(example = "987654321")
    @Pattern(regexp = "^\\+?[0-9]{7,15}$", message = "Teléfono inválido (7-15 dígitos)")
    private String phone;

    @Schema(example = "123456")
    @NotBlank(message = "La contraseña es obligatoria")
    @Size(min = 6, message = "La contraseña debe tener al menos 6 caracteres")
    private String password;
}
