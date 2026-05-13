# Cómo implementar un endpoint en SplitSnap

> Guía para el equipo. Si usas IA para ayudarte, pégale este documento como contexto antes de pedirle que escriba código. Los patrones aquí son los que ya usa el proyecto — no inventes estructura nueva.

---

## Orden obligatorio

Siempre en este orden. Saltarte un paso rompe la compilación:

```
1. model/        → la entidad JPA (tabla de BD)
2. dto/          → lo que entra y lo que sale del endpoint
3. repository/   → interfaz para hablar con la BD
4. service/      → lógica de negocio
5. controller/   → el endpoint REST
```

---

## Paso 1 — Model

Es la representación de una tabla de base de datos. Usa siempre las mismas anotaciones que el resto del proyecto.

```java
package com.splitsnap.model;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "expenses")           // nombre exacto de la tabla en BD
@Getter @Setter @NoArgsConstructor @AllArgsConstructor @Builder
public class Expense {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String description;

    @Column(nullable = false)
    private Double amount;

    // Relación con otra entidad: usa @ManyToOne + FetchType.LAZY siempre
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "group_id", nullable = false)
    private Group group;

    @Column(name = "created_at", nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    @Column(name = "updated_at", nullable = false)
    @UpdateTimestamp
    private LocalDateTime updatedAt;
}
```

**Reglas del model:**
- ID siempre es `UUID` con `@GeneratedValue(strategy = GenerationType.UUID)`
- Siempre `@Builder.Default` en campos con valor por defecto (`createdAt`, `emoji`, etc.)
- Relaciones: siempre `FetchType.LAZY` — nunca `EAGER` (causa N+1 queries)
- Nunca pongas lógica de negocio en el model

---

## Paso 2 — DTOs

Hay dos tipos: **Request** (lo que recibe el endpoint) y **Response** (lo que devuelve).

### Request DTO

```java
package com.splitsnap.dto.expense;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.Getter;
import lombok.Setter;
import java.util.List;
import java.util.UUID;

@Getter @Setter
public class CreateExpenseRequest {

    @Schema(example = "Cena de cumpleaños")   // SIEMPRE pon ejemplo — el Swagger lo muestra
    @NotBlank(message = "La descripción es obligatoria")
    private String description;

    @Schema(example = "120.00")
    @NotNull(message = "El monto es obligatorio")
    @Positive(message = "El monto debe ser mayor a 0")
    private Double amount;

    @Schema(description = "ID del usuario que pagó")
    @NotNull(message = "El campo paidBy es obligatorio")
    private UUID paidBy;

    private List<SplitEntry> splitBetween;

    @Getter @Setter
    public static class SplitEntry {
        @NotNull private UUID userId;
        @Positive private Double amount;
    }
}
```

### Response DTO

```java
package com.splitsnap.dto.expense;

import com.splitsnap.model.Expense;
import lombok.Builder;
import lombok.Getter;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter @Builder
public class ExpenseResponse {

    private UUID id;
    private String description;
    private Double amount;
    private UUID groupId;
    private UUID paidBy;
    private LocalDateTime createdAt;

    // Método estático que convierte Model → DTO
    // Siempre se llama "from" y recibe el model como parámetro
    public static ExpenseResponse from(Expense expense) {
        return ExpenseResponse.builder()
                .id(expense.getId())
                .description(expense.getDescription())
                .amount(expense.getAmount())
                .groupId(expense.getGroup().getId())
                .paidBy(expense.getPaidBy().getId())
                .createdAt(expense.getCreatedAt())
                .build();
    }
}
```

**Reglas de los DTOs:**
- Request: `@Getter @Setter`, con validaciones `@NotBlank`, `@NotNull`, `@Positive`, etc.
- Response: `@Getter @Builder`, con método estático `from(Model model)`
- **Nunca** devuelvas el Model directamente desde el controller — siempre usa un Response DTO
- **Nunca** incluyas el campo `password` en un Response DTO
- Organiza en subcarpetas: `dto/expense/`, `dto/group/`, etc.

---

## Paso 3 — Repository

Interfaz que Spring Data JPA implementa automáticamente. Solo declaras los métodos.

```java
package com.splitsnap.repository;

import com.splitsnap.model.Expense;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;
import java.util.UUID;

public interface ExpenseRepository extends JpaRepository<Expense, UUID> {

    // Opción A — nombre derivado (Spring lo resuelve automáticamente)
    List<Expense> findByGroupIdOrderByCreatedAtDesc(UUID groupId);

    // Opción B — query JPQL explícita (más segura con relaciones complejas)
    @Query("SELECT e FROM Expense e WHERE e.group.id = :groupId ORDER BY e.createdAt DESC")
    List<Expense> findExpensesByGroup(@Param("groupId") UUID groupId);
}
```

**Cuándo usar `@Query` explícita:**
- Cuando el método navega por más de una relación (`e.group.id`, `gm.user.id`)
- Cuando usas `@EmbeddedId` (claves compuestas) — los nombres derivados fallan
- Cuando la query involucra JOIN o condiciones complejas

---

## Paso 4 — Service

Aquí va toda la lógica de negocio. Nunca pongas lógica en el controller.

```java
package com.splitsnap.service;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.exception.BusinessException;
import com.splitsnap.exception.EntityNotFoundException;
import com.splitsnap.model.Expense;
import com.splitsnap.model.Group;
import com.splitsnap.repository.ExpenseRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ExpenseService {

    private final ExpenseRepository expenseRepository;
    private final GroupService groupService;      // inyecta otros services si los necesitas
    private final UserService userService;

    @Transactional                                // en métodos que escriben en BD
    public ExpenseResponse createExpense(UUID groupId, CreateExpenseRequest request, UUID userId) {

        // 1. Obtener entidades necesarias (lanza 404 si no existen)
        Group group = groupService.findById(groupId);

        // 2. Validaciones de negocio (lanza BusinessException si falla)
        if (!groupMemberRepository.existsByGroupIdAndUserId(groupId, userId)) {
            throw new BusinessException("No eres miembro de este grupo");
        }

        // 3. Construir y guardar la entidad
        Expense expense = Expense.builder()
                .description(request.getDescription())
                .amount(request.getAmount())
                .group(group)
                .build();

        expenseRepository.save(expense);

        // 4. Devolver siempre el Response DTO
        return ExpenseResponse.from(expense);
    }

    public List<ExpenseResponse> getGroupExpenses(UUID groupId) {
        return expenseRepository.findExpensesByGroup(groupId)
                .stream()
                .map(ExpenseResponse::from)
                .toList();
    }

    // Método auxiliar reutilizable — útil para otros services
    public Expense findById(UUID expenseId) {
        return expenseRepository.findById(expenseId)
                .orElseThrow(() -> new EntityNotFoundException("Gasto no encontrado"));
    }
}
```

**Reglas del service:**
- `@Transactional` solo en métodos que crean, editan o eliminan datos
- Usa `EntityNotFoundException` para "no encontrado" → devuelve **404**
- Usa `BusinessException` para reglas de negocio → devuelve **400**
- Si necesitas datos de otra entidad, inyecta su service (no su repository directo)

---

## Paso 5 — Controller

Solo recibe la request, llama al service y devuelve la response. Sin lógica.

```java
package com.splitsnap.controller;

import com.splitsnap.dto.expense.CreateExpenseRequest;
import com.splitsnap.dto.expense.ExpenseResponse;
import com.splitsnap.model.User;
import com.splitsnap.service.ExpenseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/groups/{groupId}/expenses")
@RequiredArgsConstructor
@Tag(name = "Expenses", description = "Gestión de gastos")   // agrupa en Swagger
@SecurityRequirement(name = "bearerAuth")                     // indica que requiere JWT
public class ExpenseController {

    private final ExpenseService expenseService;

    @PostMapping
    @Operation(summary = "Registrar gasto en un grupo")
    public ResponseEntity<ExpenseResponse> createExpense(
            @PathVariable UUID groupId,
            @Valid @RequestBody CreateExpenseRequest request,   // @Valid activa las validaciones del DTO
            @AuthenticationPrincipal User user) {              // usuario autenticado desde el JWT
        return ResponseEntity
                .status(HttpStatus.CREATED)                    // 201 para creación
                .body(expenseService.createExpense(groupId, request, user.getId()));
    }

    @GetMapping
    @Operation(summary = "Listar gastos de un grupo")
    public ResponseEntity<List<ExpenseResponse>> getExpenses(@PathVariable UUID groupId) {
        return ResponseEntity.ok(expenseService.getGroupExpenses(groupId));
    }
}
```

**Reglas del controller:**
- `@Valid` siempre en los `@RequestBody` — sin esto las validaciones del DTO no se ejecutan
- `@AuthenticationPrincipal User user` para obtener el usuario logueado
- Usa `ResponseEntity.status(HttpStatus.CREATED)` para POST (201), `ResponseEntity.ok()` para GET (200), `ResponseEntity.noContent()` para DELETE (204)
- El controller nunca accede directo al repository

---

## Excepciones disponibles

El proyecto ya tiene un manejador global. Solo usa estas:

| Excepción | Cuándo usarla | HTTP que devuelve |
|---|---|---|
| `EntityNotFoundException("mensaje")` | Entidad no existe en BD | 404 Not Found |
| `BusinessException("mensaje")` | Regla de negocio violada | 400 Bad Request |
| `UnauthorizedException("mensaje")` | Sin permisos para la acción | 401 Unauthorized |

```java
// Ejemplos de uso en el service:
throw new EntityNotFoundException("Gasto no encontrado");
throw new BusinessException("El monto dividido no coincide con el total");
throw new UnauthorizedException("Solo el creador puede eliminar el grupo");
```

---

## Checklist antes de hacer commit

- [ ] El model tiene `@Builder.Default` en campos con valor por defecto
- [ ] El response DTO tiene método `from(Model)` y no expone `password`
- [ ] El request DTO tiene `@Schema(example = "...")` en cada campo
- [ ] El repository usa `@Query` explícita si navega por relaciones
- [ ] El service tiene `@Transactional` en métodos de escritura
- [ ] El controller tiene `@Valid` en el `@RequestBody`
- [ ] Los errores usan `EntityNotFoundException` o `BusinessException`
- [ ] `mvn compile` pasa sin errores

---

## Estructura de carpetas de referencia

```
src/main/java/com/splitsnap/
├── controller/
│   ├── AuthController.java       ← ejemplo: endpoints públicos
│   ├── UserController.java       ← ejemplo: endpoints con JWT + avatar upload
│   └── GroupController.java      ← ejemplo: endpoints con lógica de membresía
├── service/
│   ├── AuthService.java          ← ejemplo: registro + login + BCrypt
│   ├── UserService.java          ← ejemplo: update parcial + upload de archivo
│   └── GroupService.java         ← ejemplo: @Transactional + validaciones
├── model/
│   ├── User.java                 ← ejemplo: entidad simple con UUID
│   ├── Group.java                ← ejemplo: relación @ManyToOne
│   ├── GroupMember.java          ← ejemplo: @EmbeddedId (clave compuesta)
│   └── GroupMemberId.java        ← ejemplo: Serializable para clave compuesta
├── dto/
│   ├── auth/                     ← RegisterRequest, LoginRequest, AuthResponse
│   ├── user/                     ← UpdateProfileRequest, UserResponse
│   └── group/                    ← CreateGroupRequest, GroupResponse, etc.
├── repository/
│   ├── UserRepository.java       ← ejemplo: query LIKE para búsqueda
│   └── GroupRepository.java      ← ejemplo: query con JOIN
└── exception/
    ├── GlobalExceptionHandler.java  ← no tocar
    ├── BusinessException.java
    ├── EntityNotFoundException.java
    └── UnauthorizedException.java
```

---

*SplitSnap Backend · Curso Herramientas de Desarrollo · 2026*
