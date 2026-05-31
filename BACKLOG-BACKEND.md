# BACKLOG BACKEND — SplitSnap
**Curso:** Herramientas de Desarrollo  
**Entrega:** Base de datos + Backend (Java Spring Boot + Swagger)  
**Equipo:** 5 miembros  
**Total:** 6 Épicas · 31 Historias de Usuario

---

## DISTRIBUCIÓN POR MIEMBRO

| Miembro | Épicas asignadas | Total HU |
|---------|-----------------|----------|
| Carlos Huane  | É1 Setup + É2 Autenticación + É3 Grupos | 16 HU |
| Yorma Campos  | É4 Gestión de Gastos | 5 HU |
| Dafne Fuentes| É5 Deudas y Créditos | 6 HU |
| Marcela  | É6 Swagger + Calidad (parte 1: HU-6.1, 6.2, 6.3) | 3 HU |
| Obbed | É6 Swagger + Calidad (parte 2: HU-6.4, 6.5 → tests) | 2 HU |

> **Orden de desarrollo:** É1 → É2 → É3 → É4 → É5 → É6

---

## ÉPICA 1 — Setup Inicial del Backend
**Responsable:** Carlos Huane  
**Descripción:** Configuración del proyecto Spring Boot, base de datos PostgreSQL, estructura de carpetas y dependencias base. Todos los miembros dependen de esta épica para poder trabajar.

---

### HU-1.1 — Inicializar proyecto Spring Boot
**Como** equipo de desarrollo,  
**quiero** tener un proyecto Spring Boot configurado con las dependencias necesarias,  
**para** poder desarrollar el backend de forma ordenada.

**Criterios de aceptación:**
- Proyecto creado con Spring Initializr (Java 17+, Maven)
- Dependencias incluidas: Spring Web, Spring Data JPA, Spring Security, PostgreSQL Driver, Lombok, Swagger/OpenAPI 3
- Estructura de paquetes definida: `controller/`, `service/`, `repository/`, `model/`, `dto/`, `config/`, `exception/`
- Aplicación corre en `localhost:8080` sin errores
- `application.properties` configurado con variables de entorno (sin hardcodear credenciales)

---

### HU-1.2 — Configurar base de datos PostgreSQL
**Como** equipo,  
**quiero** tener la base de datos relacional configurada y conectada al backend,  
**para** que los datos persistan correctamente.

**Criterios de aceptación:**
- Base de datos `splitsnap_db` creada en PostgreSQL
- Conexión desde Spring Boot verificada y funcional
- Hibernate configurado en modo `update`
- Script SQL de creación de tablas documentado en `/docs/schema.sql`
- Variables de entorno: `DB_URL`, `DB_USERNAME`, `DB_PASSWORD`

---

### HU-1.3 — Diseñar el esquema de base de datos
**Como** equipo,  
**quiero** tener el modelo relacional definido antes de codificar,  
**para** evitar cambios estructurales costosos durante el desarrollo.

**Criterios de aceptación:**
- Tablas creadas: `users`, `groups`, `group_members`, `expenses`, `expense_splits`, `expense_items`, `debts`, `credits`, `credit_transactions`
- Diagrama ER documentado en `/docs/er-diagram.png`
- Claves foráneas y constraints definidos correctamente
- Campos `created_at` y `updated_at` en todas las entidades principales

---

### HU-1.4 — Configurar manejo global de excepciones
**Como** desarrollador,  
**quiero** que los errores devuelvan respuestas JSON consistentes,  
**para** que el frontend pueda manejarlos de forma uniforme.

**Criterios de aceptación:**
- `@ControllerAdvice` implementado con `GlobalExceptionHandler`
- Formato estándar de error: `{ "status": 400, "error": "...", "message": "...", "timestamp": "..." }`
- Manejo de: `EntityNotFoundException` (404), `ValidationException` (400), `UnauthorizedException` (401), excepciones genéricas (500)

---

### HU-1.5 — Configurar CORS
**Como** desarrollador frontend,  
**quiero** que el backend permita peticiones desde `localhost:5173`,  
**para** poder desarrollar sin errores de CORS durante las pruebas locales.

**Criterios de aceptación:**
- CORS habilitado para `http://localhost:5173`
- Métodos permitidos: GET, POST, PUT, DELETE, OPTIONS
- Headers permitidos: `Authorization`, `Content-Type`

---

## ÉPICA 2 — Autenticación y Seguridad
**Responsable:** Carlos Huane  
**Descripción:** Sistema de registro, login con JWT y protección de endpoints. Base de la seguridad del sistema.

---

### HU-2.1 — Registrar nuevo usuario
**Como** usuario nuevo,  
**quiero** poder crear una cuenta con mi nombre, email, teléfono y contraseña,  
**para** acceder a SplitSnap.

**Criterios de aceptación:**
- `POST /api/auth/register` recibe `{ name, email, phone, password }`
- Validaciones: nombre (mín. 3 caracteres, nombre y apellido), email (formato válido y único en BD), teléfono (7-15 dígitos), contraseña (mín. 6 caracteres)
- Contraseña hasheada con BCrypt antes de guardar
- Devuelve `{ token, user: { id, name, email, phone } }`
- Si el email ya existe → 409 Conflict con mensaje descriptivo

---

### HU-2.2 — Iniciar sesión
**Como** usuario registrado,  
**quiero** poder iniciar sesión con mi email y contraseña,  
**para** acceder a mi cuenta y datos.

**Criterios de aceptación:**
- `POST /api/auth/login` recibe `{ email, password }`
- Valida credenciales contra base de datos
- Devuelve JWT con expiración de 24h: `{ token, user: { id, name, email } }`
- Si credenciales inválidas → 401 con mensaje "Credenciales incorrectas"

---

### HU-2.3 — Proteger endpoints con JWT
**Como** sistema,  
**quiero** que todos los endpoints (excepto login y register) requieran un token válido,  
**para** proteger los datos de los usuarios.

**Criterios de aceptación:**
- Filter de Spring Security valida JWT en cada request entrante
- Token enviado en header: `Authorization: Bearer <token>`
- Si token inválido o expirado → 401 con mensaje claro
- Endpoints públicos (sin token): `POST /api/auth/login`, `POST /api/auth/register`
- `userId` extraído del token disponible en controllers vía `@AuthenticationPrincipal`

---

### HU-2.4 — Obtener y editar perfil del usuario
**Como** usuario,  
**quiero** poder ver y editar mi perfil,  
**para** mantener mis datos personales actualizados.

**Criterios de aceptación:**
- `GET /api/users/me` → devuelve datos del usuario autenticado (sin campo `password`)
- `PUT /api/users/me` → actualiza `name`, `email`, `phone`, `password`
- Al cambiar contraseña: requiere `currentPassword` para verificar identidad
- Devuelve usuario actualizado

---

### HU-2.5 — Subir avatar de perfil
**Como** usuario,  
**quiero** poder subir una foto de perfil,  
**para** personalizar mi cuenta visualmente.

**Criterios de aceptación:**
- `PUT /api/users/me/avatar` recibe `multipart/form-data` con imagen
- Validación: solo imágenes (jpg, png, webp), tamaño máximo 2MB
- Guarda el archivo y devuelve URL accesible: `{ avatarUrl: "..." }`

---

### HU-2.6 — Buscar usuarios por nombre o email
**Como** usuario,  
**quiero** buscar personas por nombre o email,  
**para** poder invitarlas a mis grupos.

**Criterios de aceptación:**
- `GET /api/users/search?q={query}`
- Busca por nombre OR email (búsqueda LIKE %query%)
- Mínimo 2 caracteres para activar la búsqueda
- Excluye al usuario que realiza la búsqueda
- Devuelve: `[{ id, name, email, avatarUrl }]`

---

## ÉPICA 3 — Gestión de Grupos
**Responsable:** Carlos Huane  
**Descripción:** CRUD completo de grupos, invitación y remoción de miembros, visualización de balances.

---

### HU-3.1 — Crear grupo
**Como** usuario,  
**quiero** crear un grupo con nombre y emoji,  
**para** organizar gastos compartidos con otras personas.

**Criterios de aceptación:**
- `POST /api/groups` recibe `{ name, emoji, memberIds[] }`
- El creador se agrega automáticamente como miembro
- Crea entradas en `group_members` para todos los memberIds enviados
- Devuelve `{ id, name, emoji, createdBy, createdAt, members[] }`
- Nombre requerido; emoji opcional (default: "📦")

---

### HU-3.2 — Listar mis grupos
**Como** usuario,  
**quiero** ver todos los grupos en los que participo,  
**para** gestionar mis gastos compartidos desde el dashboard.

**Criterios de aceptación:**
- `GET /api/groups` → lista grupos donde el usuario autenticado es miembro
- Cada grupo incluye: `{ id, name, emoji, memberCount, myBalance }`
- `myBalance`: monto positivo si me deben, negativo si debo
- Ordenados por actividad más reciente

---

### HU-3.3 — Ver detalle de un grupo
**Como** usuario,  
**quiero** ver el detalle de un grupo con balances y gastos recientes,  
**para** entender el estado financiero del grupo.

**Criterios de aceptación:**
- `GET /api/groups/{groupId}`
- Si el usuario no es miembro del grupo → 403 Forbidden
- Devuelve: `{ id, name, emoji, members[], recentExpenses[], memberBalances[] }`
- `memberBalances`: balance neto por persona `{ userId, name, balance }`
- `recentExpenses`: últimos 10 gastos del grupo

---

### HU-3.4 — Invitar miembro a un grupo
**Como** miembro de un grupo,  
**quiero** poder agregar más personas al grupo,  
**para** incluirlas en los gastos compartidos.

**Criterios de aceptación:**
- `POST /api/groups/{groupId}/members` recibe `{ userId }`
- Valida que quien invita sea miembro del grupo
- Valida que el usuario invitado no sea ya miembro → 409 si ya existe
- Si userId no existe → 404

---

### HU-3.5 — Eliminar miembro de un grupo
**Como** creador del grupo,  
**quiero** poder remover miembros del grupo,  
**para** gestionar correctamente la composición del grupo.

**Criterios de aceptación:**
- `DELETE /api/groups/{groupId}/members/{userId}`
- Solo el creador del grupo puede remover miembros → 403 si otro lo intenta
- No se puede remover a un miembro con deudas pendientes en el grupo
- El creador no puede removerse a sí mismo

---

## ÉPICA 4 — Gestión de Gastos
**Responsable:** Miembro 2 (por asignar)  
**Descripción:** Registro de gastos manuales y desde OCR, lógica de división y cálculo automático de deudas.

---

### HU-4.1 — Registrar gasto manual
**Como** miembro de un grupo,  
**quiero** registrar un gasto indicando quién pagó y cómo se divide,  
**para** que el sistema calcule las deudas automáticamente.

**Criterios de aceptación:**
- `POST /api/groups/{groupId}/expenses` recibe:
```json
{
  "description": "Cena",
  "amount": 120.00,
  "paidBy": "userId",
  "splitBetween": [
    { "userId": "u2", "amount": 60.0 },
    { "userId": "u3", "amount": 60.0 }
  ],
  "date": "2026-05-12",
  "items": []
}
```
- Validaciones: `amount > 0`, `description` no vacía, suma de `splitBetween` ≈ `amount` (tolerancia 0.01)
- Genera automáticamente las deudas en tabla `debts`
- Devuelve `{ expense, debts[] }`

---

### HU-4.2 — Calcular y generar deudas automáticamente
**Como** sistema,  
**quiero** calcular quién le debe a quién al registrar un gasto,  
**para** que los usuarios no tengan que hacerlo manualmente.

**Criterios de aceptación:**
- Lógica encapsulada en `ExpenseService.buildDebts(expense)`
- Por cada entrada en `splitBetween`: si `userId != paidBy` y `amount > 0` → crear deuda `fromUser → paidBy`
- Deudas creadas con `status = PENDING`
- El pagador no genera deuda hacia sí mismo
- Tolerancia de redondeo: diferencia máxima de S/ 0.01 entre suma de splits y total

---

### HU-4.3 — Listar gastos de un grupo
**Como** miembro,  
**quiero** ver todos los gastos de un grupo,  
**para** llevar un registro claro de los gastos compartidos.

**Criterios de aceptación:**
- `GET /api/groups/{groupId}/expenses`
- Solo miembros del grupo pueden ver los gastos → 403 si no es miembro
- Devuelve: `[{ id, description, amount, paidBy, date, splitBetween[] }]`
- Ordenados por fecha descendente

---

### HU-4.4 — Ver detalle de un gasto
**Como** miembro,  
**quiero** ver el detalle completo de un gasto específico,  
**para** entender exactamente cómo se dividió.

**Criterios de aceptación:**
- `GET /api/groups/{groupId}/expenses/{expenseId}`
- Incluye: datos del gasto + splits + items (si es de recibo OCR) + deudas generadas
- 404 si el gasto no existe

---

### HU-4.5 — Registrar gasto desde escaneo de recibo (OCR)
**Como** usuario,  
**quiero** poder escanear un recibo y asignar cada ítem a un miembro,  
**para** dividir gastos de forma precisa sin cálculo manual.

**Criterios de aceptación:**
- `POST /api/ocr/scan` recibe imagen vía `multipart/form-data`
- Integra con Google Cloud Vision API
- Devuelve: `{ items: [{ name, price, quantity }] }`
- Si el OCR falla → 500 con mensaje descriptivo
- Los ítems son revisables en el frontend antes de confirmar el gasto

---

## ÉPICA 5 — Deudas, Pagos y Créditos
**Responsable:** Miembro 3 (por asignar)  
**Descripción:** Gestión de deudas pendientes, sistema de créditos internos para pago y generación del historial de transacciones.

---

### HU-5.1 — Ver deudas de un grupo
**Como** miembro,  
**quiero** ver todas las deudas pendientes y pagadas del grupo,  
**para** saber cuánto me deben o cuánto debo.

**Criterios de aceptación:**
- `GET /api/groups/{groupId}/debts`
- Devuelve deudas `PENDING` y `PAID`
- Cada deuda: `{ id, fromUser, toUser, amount, status, expenseId, paidAt, paidWith }`
- Filtro opcional por estado: `?status=PENDING` o `?status=PAID`

---

### HU-5.2 — Marcar deuda como pagada
**Como** usuario deudor,  
**quiero** marcar una deuda como pagada indicando el método de pago,  
**para** registrar que saldé la deuda con el acreedor.

**Criterios de aceptación:**
- `PUT /api/groups/{groupId}/debts/{debtId}/mark-paid` recibe `{ paidWith: "yape|paypal|efectivo" }`
- Solo el deudor (`fromUser`) puede marcar como pagado → 403 si otro lo intenta
- Actualiza: `status = PAID`, `paidAt = now()`, `paidWith = método`
- Devuelve la deuda actualizada

---

### HU-5.3 — Pagar deuda con créditos del sistema
**Como** usuario,  
**quiero** usar mis créditos de SplitSnap para pagar una deuda,  
**para** saldar deudas sin salir de la aplicación.

**Criterios de aceptación:**
- `PUT /api/groups/{groupId}/debts/{debtId}/pay-credits`
- Valida que el usuario tenga créditos suficientes → 400 si no alcanza
- Descuenta créditos del balance del usuario
- Registra en `credit_transactions` con `type = SPEND`
- Marca deuda como `PAID` con `paidWith = "credits"`

---

### HU-5.4 — Ver balance de créditos
**Como** usuario,  
**quiero** ver cuántos créditos tengo y el historial de compras y gastos,  
**para** gestionar mi saldo de créditos.

**Criterios de aceptación:**
- `GET /api/users/me/credits` → `{ balance, transactions[] }`
- `transactions`: `{ id, type, amount, debtId, date }`
- Tipos posibles: `PURCHASE` (compra de créditos) y `SPEND` (pago de deuda)

---

### HU-5.5 — Comprar créditos
**Como** usuario,  
**quiero** comprar créditos en paquetes predefinidos,  
**para** poder pagar deudas dentro de la aplicación.

**Criterios de aceptación:**
- `POST /api/users/me/credits/buy` recibe `{ amount }` (monto en soles, 1 crédito = S/ 1)
- Paquetes válidos: 10, 25, 50, 100 (o monto personalizado > 0)
- Suma el monto al balance del usuario
- Registra en `credit_transactions` con `type = PURCHASE`
- Devuelve `{ newBalance, transaction }`

---

### HU-5.6 — Ver historial de transacciones
**Como** usuario,  
**quiero** ver un historial de todos mis gastos y pagos,  
**para** llevar un registro completo de mi actividad financiera.

**Criterios de aceptación:**
- `GET /api/users/me/transactions`
- Combina gastos creados + deudas pagadas del usuario autenticado
- Filtros opcionales: `?groupId=`, `?type=expense|payment`, `?from=`, `?to=`
- Cada entrada: `{ id, type, description, group, amount, date }`
- Ordenados por fecha descendente

---

## ÉPICA 6 — Documentación con Swagger y Calidad
**Responsable:** Miembros 4 y 5 (por asignar)  
**Descripción:** Documentación completa de la API con Swagger/OpenAPI 3 y pruebas de los flujos principales.

---

### HU-6.1 — Configurar Swagger / OpenAPI 3
**Como** equipo,  
**quiero** tener la documentación de la API generada automáticamente,  
**para** que el docente y el equipo puedan explorar todos los endpoints.

**Criterios de aceptación:**
- Dependencia `springdoc-openapi-starter-webmvc-ui` configurada en `pom.xml`
- Swagger UI accesible en `http://localhost:8080/swagger-ui.html`
- Todos los endpoints listados y accesibles desde la UI
- Endpoints agrupados por tag: `Auth`, `Users`, `Groups`, `Expenses`, `Debts`, `Credits`

---

### HU-6.2 — Documentar modelos y endpoints con anotaciones
**Como** desarrollador,  
**quiero** que los DTOs y controllers tengan anotaciones Swagger descriptivas,  
**para** que la documentación sea clara y útil.

**Criterios de aceptación:**
- `@Schema(description = "...")` en todos los campos de DTOs
- `@Operation(summary = "...", description = "...")` en todos los métodos de controllers
- `@ApiResponse` para respuestas exitosas y de error en cada endpoint
- Ejemplos de JSON en los schemas principales

---

### HU-6.3 — Escribir tests de integración para autenticación
**Como** equipo,  
**quiero** tener tests del flujo de autenticación,  
**para** verificar que el sistema de login y registro funciona correctamente.

**Criterios de aceptación:**
- Tests cubiertos: register exitoso, register con email duplicado, login exitoso, login con credenciales inválidas, acceso a endpoint protegido sin token
- Usa `@SpringBootTest` + `MockMvc`
- Base de datos H2 en memoria para no afectar la BD real
- Todos los tests pasan sin errores

---

### HU-6.4 — Escribir tests unitarios para lógica de gastos
**Como** equipo,  
**quiero** tests unitarios para la lógica de división de gastos,  
**para** garantizar que los cálculos de deudas son correctos.

**Criterios de aceptación:**
- Tests para `buildDebts()`: división igual, división personalizada, tolerancia de redondeo
- Tests para validaciones de gasto: monto 0, descripción vacía, splits que no suman el total
- Cobertura mínima del `ExpenseService`: 80%

---

## RESUMEN DEL BACKLOG

| # | Épica | Responsable | HU | Prioridad |
|---|-------|-------------|-----|-----------|
| É1 | Setup Inicial del Backend | Carlos Huane | 5 | CRÍTICA |
| É2 | Autenticación y Seguridad | Carlos Huane | 6 | CRÍTICA |
| É3 | Gestión de Grupos | Carlos Huane | 5 | ALTA |
| É4 | Gestión de Gastos | Miembro 2 | 5 | ALTA |
| É5 | Deudas, Pagos y Créditos | Miembro 3 | 6 | MEDIA |
| É6 | Swagger y Calidad | Miembros 4 y 5 | 4 | MEDIA |
| | **TOTAL** | | **31 HU** | |

---

## ENDPOINTS COMPLETOS DEL BACKEND

```
=== AUTH ===
POST   /api/auth/register
POST   /api/auth/login

=== USERS ===
GET    /api/users/me
PUT    /api/users/me
PUT    /api/users/me/avatar
GET    /api/users/search?q={query}
GET    /api/users/me/credits
POST   /api/users/me/credits/buy
GET    /api/users/me/transactions

=== GROUPS ===
GET    /api/groups
POST   /api/groups
GET    /api/groups/{groupId}
POST   /api/groups/{groupId}/members
DELETE /api/groups/{groupId}/members/{userId}

=== EXPENSES ===
GET    /api/groups/{groupId}/expenses
POST   /api/groups/{groupId}/expenses
GET    /api/groups/{groupId}/expenses/{expenseId}

=== DEBTS ===
GET    /api/groups/{groupId}/debts
PUT    /api/groups/{groupId}/debts/{debtId}/mark-paid
PUT    /api/groups/{groupId}/debts/{debtId}/pay-credits

=== OCR ===
POST   /api/ocr/scan
```

---

*Documento generado: 2026-05-12 | SplitSnap Backend Sprint 1*
