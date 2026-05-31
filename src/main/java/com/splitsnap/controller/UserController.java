package com.splitsnap.controller;

import com.splitsnap.dto.user.UpdateProfileRequest;
import com.splitsnap.dto.user.UserResponse;
import com.splitsnap.model.User;
import com.splitsnap.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
@Tag(name = "Users", description = "Perfil y búsqueda de usuarios")
@SecurityRequirement(name = "bearerAuth")
/**
 * @apiDefine UsersGroup Perfil, búsqueda y avatar del usuario autenticado.
 */
public class UserController {

    private final UserService userService;

    @GetMapping("/me")
    @Operation(
        summary = "Obtener perfil del usuario autenticado",
        description = "Devuelve los datos del usuario asociado al JWT (sin password)"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil obtenido correctamente"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {get} /api/users/me Obtener perfil autenticado
     * @apiName GetProfile
     * @apiGroup Users
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     */
    public ResponseEntity<UserResponse> getProfile(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getProfile(user.getId()));
    }

    @PutMapping("/me")
    @Operation(
        summary = "Actualizar perfil del usuario autenticado",
        description = "Permite editar nombre, email, teléfono y contraseña del usuario actual"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Perfil actualizado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos o contraseña actual incorrecta"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado"),
        @ApiResponse(responseCode = "409", description = "El email ya está en uso")
    })
    /**
     * @api {put} /api/users/me Actualizar perfil autenticado
     * @apiName UpdateProfile
     * @apiGroup Users
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} name Nombre completo.
     * @apiParam {String} email Correo.
     * @apiParam {String} [phone] Teléfono.
     * @apiParam {String} [currentPassword] Contraseña actual.
     * @apiParam {String} [newPassword] Nueva contraseña.
     */
    public ResponseEntity<UserResponse> updateProfile(
            @AuthenticationPrincipal User user,
            @Valid @RequestBody UpdateProfileRequest request) {
        return ResponseEntity.ok(userService.updateProfile(user.getId(), request));
    }

    @PutMapping("/me/avatar")
    @Operation(
        summary = "Subir foto de perfil",
        description = "Sube una imagen (jpg/png/webp, máx 2MB) como avatar del usuario autenticado"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Avatar actualizado, devuelve la URL"),
        @ApiResponse(responseCode = "400", description = "Archivo inválido o supera el tamaño máximo"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {put} /api/users/me/avatar Subir avatar
     * @apiName UploadAvatar
     * @apiGroup Users
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {File} file Archivo de imagen.
     */
    public ResponseEntity<Map<String, String>> uploadAvatar(
            @AuthenticationPrincipal User user,
            @RequestParam("file") MultipartFile file) throws IOException {
        String avatarUrl = userService.uploadAvatar(user.getId(), file);
        return ResponseEntity.ok(Map.of("avatarUrl", avatarUrl));
    }

    @GetMapping("/search")
    @Operation(
        summary = "Buscar usuarios por nombre o email",
        description = "Devuelve los usuarios que coinciden con el query (mínimo 2 caracteres). Excluye al usuario autenticado."
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Búsqueda realizada correctamente"),
        @ApiResponse(responseCode = "400", description = "El query tiene menos de 2 caracteres"),
        @ApiResponse(responseCode = "401", description = "Usuario no autenticado")
    })
    /**
     * @api {get} /api/users/search Buscar usuarios
     * @apiName SearchUsers
     * @apiGroup Users
     * @apiVersion 1.0.0
     * @apiHeader {String} Authorization Bearer JWT.
     * @apiParam {String} q Texto de búsqueda.
     */
    public ResponseEntity<List<UserResponse>> search(
            @RequestParam String q,
            @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.search(q, user.getId()));
    }
}
