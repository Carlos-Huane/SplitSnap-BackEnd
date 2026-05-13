# SplitSnap — Backend

API REST para SplitSnap, app de gestión de gastos compartidos.  
**Stack:** Java 17 · Spring Boot 3.2 · PostgreSQL · JWT · Swagger/OpenAPI 3

---

## Requisitos previos

Instalar antes de empezar:

| Herramienta | Versión mínima | Descarga |
|-------------|---------------|----------|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| PostgreSQL | 15+ | https://www.postgresql.org/download |
| Git | cualquiera | https://git-scm.com |

> Puedes verificar las versiones con: `java -version`, `mvn -version`, `psql --version`

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/Carlos-Huane/SplitSnap-.git
cd SplitSnap-BackEnd
```

---

## 2. Crear la base de datos en PostgreSQL

Abre pgAdmin o la terminal de PostgreSQL y ejecuta:

```sql
CREATE DATABASE splitsnap_db;
```

No necesitas crear las tablas manualmente. Spring Boot las genera automáticamente al levantar el servidor.

---

## 3. Configurar variables de entorno (método VS Code — recomendado)

El proyecto incluye `.vscode/launch.json.example`. Cópialo y renómbralo:

```
cp .vscode/launch.json.example .vscode/launch.json
```

Abre `.vscode/launch.json` y reemplaza `TU_PASSWORD_POSTGRES_AQUI` con tu contraseña de PostgreSQL. Este archivo está en `.gitignore`, así que no lo vas a subir accidentalmente.

> Instala las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack** en VS Code (búscalas en `Ctrl+Shift+X`).

### Alternativa — variables por CMD

Si prefieres no usar VS Code, en CMD ejecuta primero:

```
set DB_URL=jdbc:postgresql://localhost:5432/splitsnap_db
set DB_USERNAME=postgres
set DB_PASSWORD=tu_password_postgres
set JWT_SECRET=splitsnap_clave_secreta_2026_desarrollo
```

Estas variables solo duran mientras esa ventana de CMD esté abierta.

---

## 4. Levantar el servidor

### Con VS Code (recomendado)

Presiona `F5` o usa el **Spring Boot Dashboard** (pestaña con ícono de hoja verde) → botón ▶ junto a `splitsnap-backend`.

### Con CMD

```
mvn spring-boot:run
```

En ambos casos verás:

```
Started SplitSnapApplication in 3.2 seconds
Tomcat started on port 8080
```

El servidor queda corriendo en `http://localhost:8080`.

> ⚠️ Si clonaste el proyecto en una ruta con caracteres especiales (emojis, tildes, espacios) y usas CMD, el JAR puede fallar. Consulta `docs/SETUP-LOCAL.md` para el workaround.

---

## 5. Verificar que funciona

Abre el navegador o Postman:

| URL | Qué muestra |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Documentación interactiva de todos los endpoints |
| `http://localhost:8080/api-docs` | JSON de la API (OpenAPI 3) |

---

## 6. Crear tu endpoint (flujo de trabajo)

Cada historia de usuario es un commit. El flujo por cada endpoint nuevo:

### 6.1 Crear la rama desde develop

```bash
git checkout develop
git pull
git checkout -b feature/nombre-de-tu-epica
```

### 6.2 Estructura de paquetes

```
src/main/java/com/splitsnap/
├── model/          ← @Entity (tabla de BD)
├── dto/            ← objetos de request y response (sin @Entity)
├── repository/     ← interface que extiende JpaRepository
├── service/        ← lógica de negocio (@Service)
└── controller/     ← endpoints REST (@RestController)
```

### 6.3 Orden recomendado al crear un endpoint

```
1. model/MiEntidad.java        → @Entity con los campos de la BD
2. dto/MiRequest.java          → lo que recibe el endpoint
3. dto/MiResponse.java         → lo que devuelve el endpoint
4. repository/MiRepository.java → interface JpaRepository<MiEntidad, UUID>
5. service/MiService.java       → lógica (validaciones, cálculos)
6. controller/MiController.java → @RestController con el @PostMapping/@GetMapping
```

### 6.4 Ejemplo mínimo

```java
// model/User.java
@Entity @Table(name = "users")
@Getter @Setter @NoArgsConstructor
public class User {
    @Id @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;
    private String name;
    private String email;
}

// repository/UserRepository.java
public interface UserRepository extends JpaRepository<User, UUID> {
    Optional<User> findByEmail(String email);
}

// service/UserService.java
@Service @RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;

    public User findById(UUID id) {
        return userRepository.findById(id)
            .orElseThrow(() -> new EntityNotFoundException("Usuario no encontrado"));
    }
}

// controller/UserController.java
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {
    private final UserService userService;

    @GetMapping("/{id}")
    public ResponseEntity<User> getUser(@PathVariable UUID id) {
        return ResponseEntity.ok(userService.findById(id));
    }
}
```

### 6.5 Commit y PR

```bash
git add .
git commit -m "feat: descripción de lo que hiciste"
git push -u origin feature/nombre-de-tu-epica
```

Luego crea el PR en GitHub: `feature/tu-rama` → `develop`.

---

## Estructura del proyecto

```
splitsnap-backend/
├── docs/
│   └── schema.sql              ← Esquema de referencia de la BD
├── src/
│   ├── main/
│   │   ├── java/com/splitsnap/
│   │   │   ├── SplitSnapApplication.java
│   │   │   ├── config/         ← CORS, Security, Swagger
│   │   │   ├── controller/     ← Endpoints REST
│   │   │   ├── dto/            ← Request y Response objects
│   │   │   ├── exception/      ← Excepciones personalizadas
│   │   │   ├── model/          ← Entidades JPA
│   │   │   ├── repository/     ← Interfaces JpaRepository
│   │   │   └── service/        ← Lógica de negocio
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       ├── java/com/splitsnap/
│       └── resources/
│           └── application.properties  ← Usa H2 en memoria para tests
├── pom.xml
└── README.md
```

---

## Convenciones del equipo

### Ramas
```
feature/<nombre-epica>   → nueva épica
fix/<descripcion>        → corrección de bug
```

### Commits (Conventional Commits)
```
feat:     nuevo endpoint o funcionalidad
fix:      corrección de bug
refactor: cambio de estructura sin cambiar comportamiento
chore:    dependencias, configuración
```

### Errores — formato estándar
Todos los errores devuelven este JSON:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Usuario no encontrado",
  "timestamp": "2026-05-12T23:00:00"
}
```

---

## Ramas del equipo

| Rama | Responsable | Épica |
|------|-------------|-------|
| `feature/setup-inicial-backend` | Carlos Huane | É1 — Setup |
| `feature/autenticacion-backend` | Carlos Huane | É2 — Auth |
| `feature/grupos-backend` | Carlos Huane | É3 — Grupos |
| `feature/gastos-backend` | Yorma Campos | É4 — Gastos |
| `feature/deudas-backend` | Dafne Fuentes | É5 — Deudas |
| `feature/swagger-backend` | Marcela / Obbed | É6 — Swagger |

---

*SplitSnap Backend · Curso Herramientas de Desarrollo · 2026*
