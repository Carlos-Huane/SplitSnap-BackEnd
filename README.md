# SplitSnap — Backend

API REST para SplitSnap, app de gestión de gastos compartidos.  
**Stack:** Java 17 · Spring Boot 3.2 · MySQL · JWT · Swagger/OpenAPI 3

---

## Requisitos previos

| Herramienta | Versión mínima | Descarga |
|-------------|----------------|----------|
| Java JDK | 17 | https://adoptium.net |
| Maven | 3.9+ | https://maven.apache.org |
| MySQL | 8.0+ | https://dev.mysql.com/downloads/installer |
| Git | cualquiera | https://git-scm.com |
| DBeaver (opcional) | cualquiera | https://dbeaver.io |

> Verifica con: `java -version`, `mvn -version`

---

## 1. Clonar el repositorio

```bash
git clone https://github.com/Carlos-Huane/SplitSnap-BackEnd.git
cd SplitSnap-BackEnd
```

---

## 2. Configurar credenciales locales

Las credenciales no están en el repo. Cada desarrollador crea su propio `.env`:

1. Copia `.env.example` → `.env`:
   ```
   copy .env.example .env
   ```
2. Abre el `.env` y reemplaza `TU_PASSWORD_DE_MYSQL` con tu contraseña de MySQL

> ⚠️ El `.env` está en `.gitignore` — nunca lo subas al repo.

---

## 3. Configurar VS Code

1. Instala las extensiones **Extension Pack for Java** y **Spring Boot Extension Pack** (`Ctrl+Shift+X`)
2. Copia `.vscode/launch.json.example` → `.vscode/launch.json`:
   ```
   copy .vscode\launch.json.example .vscode\launch.json
   ```
   No necesitas editar nada — ya apunta al `.env`.

---

## 4. Levantar el servidor

Presiona `F5` o usa el **Spring Boot Dashboard** → botón ▶ junto a `splitsnap-backend`.

Cuando veas esto, el servidor está listo:

```
Started SplitSnapApplication in X.XXX seconds
Tomcat started on port 8080
```

El navegador se abre automáticamente en Swagger. Si no ocurre, entra a `http://localhost:8080/swagger-ui.html`.

> La primera vez que arranca, Hibernate crea las tablas automáticamente y se insertan datos de prueba.

---

## 3. Verificar con Swagger

| URL | Qué muestra |
|-----|-------------|
| `http://localhost:8080/swagger-ui.html` | Documentación interactiva |
| `http://localhost:8080/api-docs` | JSON OpenAPI 3 |

---

## 4. Datos de prueba

Al arrancar por primera vez se crean estos registros:

**Usuarios** (todos con password `test123`):

| Nombre | Email |
|--------|-------|
| Carlos Huane | carlos@splitsnap.com |
| Ana Torres | ana@splitsnap.com |
| Juan Paredes | juan@splitsnap.com |

**Grupos:**

| Grupo | Creador | Miembros |
|-------|---------|----------|
| Viaje a Cusco 🏔️ | Carlos | Carlos, Ana, Juan |
| Departamento 🏠 | Ana | Ana, Carlos |

---

## 5. Ver las tablas con DBeaver

1. Abre DBeaver → clic en el enchufe con `+` → **New Database Connection**
2. Selecciona **MySQL** → Next
3. Completa:
   - Server Host: `localhost`
   - Port: `3306`
   - Database: `splitsnap_db`
   - Username: `root`
   - Password: `admin`
4. Clic en **Test Connection** (descarga el driver si te lo pide) → Finish

Ya puedes ver las tablas `users`, `groups` y `group_members` con los datos iniciales.

---

## 6. Crear tu endpoint (flujo de trabajo)

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
├── dto/            ← objetos de request y response
├── repository/     ← interface JpaRepository
├── service/        ← lógica de negocio
└── controller/     ← endpoints REST
```

### 6.3 Orden recomendado

```
1. model/MiEntidad.java         → @Entity con los campos
2. dto/MiRequest.java           → lo que recibe el endpoint
3. dto/MiResponse.java          → lo que devuelve el endpoint
4. repository/MiRepository.java → JpaRepository<MiEntidad, UUID>
5. service/MiService.java       → validaciones y lógica
6. controller/MiController.java → @RestController con los mappings
```

### 6.4 Commit y PR

```bash
git add .
git commit -m "feat: descripción de lo que hiciste"
git push -u origin feature/nombre-de-tu-epica
```

Crea el PR en GitHub: `feature/tu-rama` → `develop`.

---

## Estructura del proyecto

```
splitsnap-backend/
├── docs/
│   ├── schema.sql                  ← Esquema de referencia
│   ├── SETUP-LOCAL.md              ← Guía de instalación detallada
│   └── COMO-IMPLEMENTAR-ENDPOINT.md
├── src/
│   ├── main/
│   │   ├── java/com/splitsnap/
│   │   │   ├── config/             ← CORS, Security, Swagger, DataInitializer
│   │   │   ├── controller/
│   │   │   ├── dto/
│   │   │   ├── exception/
│   │   │   ├── model/
│   │   │   ├── repository/
│   │   │   └── service/
│   │   └── resources/
│   │       └── application.properties
│   └── test/
│       └── resources/
│           └── application.properties  ← H2 en memoria para tests
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
