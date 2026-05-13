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

## 3. Configurar variables de entorno

Crea un archivo `.env` en la raíz del proyecto (ya está en `.gitignore`, nunca lo subas):

```env
DB_URL=jdbc:postgresql://localhost:5432/splitsnap_db
DB_USERNAME=postgres
DB_PASSWORD=tu_contraseña_postgres
JWT_SECRET=cualquier_texto_largo_secreto_minimo_32_caracteres
```

Reemplaza `tu_contraseña_postgres` con la contraseña que pusiste al instalar PostgreSQL.

### Alternativa — pasar las variables al correr Maven

Si prefieres no crear el archivo `.env`, puedes pasar las variables directo:

```bash
DB_URL=jdbc:postgresql://localhost:5432/splitsnap_db \
DB_USERNAME=postgres \
DB_PASSWORD=tu_password \
JWT_SECRET=mi_secreto_largo \
mvn spring-boot:run
```

---

## 4. Levantar el servidor

```bash
mvn spring-boot:run
```

La primera vez Maven descarga las dependencias (puede tardar 2-3 minutos). Las siguientes veces arranca en segundos.

Verás en la consola algo como:

```
Started SplitSnapApplication in 3.2 seconds
Tomcat started on port 8080
```

El servidor queda corriendo en `http://localhost:8080`.

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
