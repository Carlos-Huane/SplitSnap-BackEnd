package com.splitsnap.controller;

import com.splitsnap.dto.auth.AuthResponse;
import com.splitsnap.dto.auth.LoginRequest;
import com.splitsnap.dto.auth.RegisterRequest;
import com.splitsnap.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "Auth", description = "Registro e inicio de sesión")
/**
 * @apiDefine AuthGroup Autenticación y registro.
 */
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    @Operation(
        summary = "Registrar nuevo usuario",
        description = "Crea una nueva cuenta y devuelve un JWT junto con los datos básicos del usuario"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "Usuario registrado correctamente"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos (validaciones)"),
        @ApiResponse(responseCode = "409", description = "El email ya está registrado")
    })
    /**
     * @api {post} /api/auth/register Registrar nuevo usuario
     * @apiName RegisterUser
     * @apiGroup Auth
     * @apiVersion 1.0.0
     * @apiParam {String} name Nombre completo.
     * @apiParam {String} email Correo del usuario.
     * @apiParam {String} [phone] Teléfono opcional.
     * @apiParam {String} password Contraseña.
     * @apiSuccess {String} token JWT de acceso.
     * @apiSuccess {Object} user Usuario registrado.
     */
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(authService.register(request));
    }

    @PostMapping("/login")
    @Operation(
        summary = "Iniciar sesión",
        description = "Valida credenciales y devuelve un JWT con expiración de 24h"
    )
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "Inicio de sesión exitoso"),
        @ApiResponse(responseCode = "400", description = "Datos inválidos (validaciones)"),
        @ApiResponse(responseCode = "401", description = "Credenciales incorrectas")
    })
    /**
     * @api {post} /api/auth/login Iniciar sesión
     * @apiName LoginUser
     * @apiGroup Auth
     * @apiVersion 1.0.0
     * @apiParam {String} email Correo del usuario.
     * @apiParam {String} password Contraseña.
     * @apiSuccess {String} token JWT de acceso.
     * @apiSuccess {Object} user Usuario autenticado.
     */
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        return ResponseEntity.ok(authService.login(request));
    }
}
