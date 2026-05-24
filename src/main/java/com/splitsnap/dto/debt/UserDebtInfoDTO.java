package com.splitsnap.dto.debt;

import java.util.UUID;

public class UserDebtInfoDTO {
    // Lo mantenemos como String para que el Frontend reciba texto plano
    private String id;
    private String name;
    private String email;
    private String avatarUrl;

    // Modificamos el constructor para aceptar el UUID de tu entidad User
    public UserDebtInfoDTO(UUID id, String name, String email, String avatarUrl) {
        // Hacemos la conversión a String aquí mismo de forma segura
        this.id = id != null ? id.toString() : null;
        this.name = name;
        this.email = email;
        this.avatarUrl = avatarUrl;
    }

    // ── GETTERS Y SETTERS ─────────────────────────────────────────
    public String getId() { return id; }
    public void setId(String id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAvatarUrl() { return avatarUrl; }
    public void setAvatarUrl(String avatarUrl) { this.avatarUrl = avatarUrl; }
}