# Guía de Setup Local — SplitSnap Backend
> Sigue esta guía en orden. No saltes pasos.

---

## PASO 1 — Instalar Java 17+

1. Descarga **Eclipse Temurin JDK 21** desde: https://adoptium.net
2. Ejecuta el instalador `.msi` (todo por defecto)
3. Verifica en CMD:
```
java -version
```
Debe mostrar `openjdk version "21.x.x"` o similar.

---

## PASO 2 — Instalar Maven

1. Descarga **Apache Maven 3.9+** desde: https://maven.apache.org/download.cgi  
   → Elige **Binary zip archive** (el `.zip`, no el source)

2. Descomprime el zip. Ejemplo: `C:\Users\TU_USUARIO\maven\apache-maven-3.9.x`

3. Agrega Maven al PATH:
   - Busca **"Variables de entorno"** en el menú inicio
   - **"Editar las variables de entorno del sistema"** → **"Variables de entorno..."**
   - En **"Variables del sistema"** → busca `Path` → doble clic → **"Nuevo"**
   - Pega la ruta a la carpeta `bin` de Maven:
     ```
     C:\Users\TU_USUARIO\maven\apache-maven-3.9.x\bin
     ```
   - Acepta en todas las ventanas

4. **Cierra y vuelve a abrir CMD** y verifica:
```
mvn --version
```

---

## PASO 3 — Instalar MySQL

1. Descarga **MySQL Installer** desde: https://dev.mysql.com/downloads/installer
2. Ejecuta el instalador → elige **"Developer Default"**
3. Durante la configuración:
   - Anota bien el **Root password** que pongas — lo necesitarás después
   - Puerto por defecto: `3306` → déjalo así
4. Completa la instalación

---

## PASO 4 — Instalar DBeaver (para ver las tablas)

DBeaver es un cliente visual para explorar y consultar la base de datos.

1. Descarga desde: https://dbeaver.io/download
2. Instala con todas las opciones por defecto

### Conectar DBeaver a splitsnap_db

> Primero levanta el backend al menos una vez (Paso 7) para que la base de datos sea creada automáticamente.

1. Abre DBeaver
2. Clic en el enchufe con `+` (esquina superior izquierda) → **New Database Connection**
3. Selecciona **MySQL** → Next
4. Completa los campos:
   - Server Host: `localhost`
   - Port: `3306`
   - Database: `splitsnap_db`
   - Username: `root`
   - Password: la que pusiste al instalar MySQL
5. Clic en **Test Connection** → si te pide descargar el driver MySQL, acepta → **Finish**

Ahora puedes ver las tablas `users`, `groups` y `group_members` con los datos de prueba.

---

## PASO 5 — Clonar el repositorio

Abre **CMD** y ejecuta:
```
git clone https://github.com/Carlos-Huane/SplitSnap-BackEnd.git
```

> ⚠️ Evita clonar en rutas con caracteres especiales (emojis, tildes, paréntesis). Usa rutas simples como `C:\Users\TU_USUARIO\Desktop\SplitSnap-BackEnd`.

---

## PASO 6 — Configurar tus credenciales locales

Las credenciales **no están en el repo** por seguridad. Cada desarrollador tiene su propio archivo `.env` local.

1. En la raíz del proyecto clonado, busca el archivo **`.env.example`**
2. **Cópialo** y renómbralo a **`.env`**  
   (en CMD: `copy .env.example .env`)
3. Abre el `.env` con cualquier editor y reemplaza `TU_PASSWORD_DE_MYSQL` con tu contraseña de MySQL:

```
DB_URL=jdbc:mysql://localhost:3306/splitsnap_db?createDatabaseIfNotExist=true&useSSL=false&serverTimezone=America/Lima
DB_USERNAME=root
DB_PASSWORD=tu_password_aqui
```

> ⚠️ **Nunca subas el `.env` al repo** — ya está en `.gitignore`, pero asegúrate de no agregarlo manualmente.

---

## PASO 7 — Configurar VS Code

1. Instala las extensiones (`Ctrl+Shift+X`):
   - **Extension Pack for Java** — de Microsoft
   - **Spring Boot Extension Pack** — de VMware

2. Dentro de la carpeta `.vscode/` hay un archivo **`launch.json.example`** — cópialo y renómbralo a **`launch.json`**:
   ```
   copy .vscode\launch.json.example .vscode\launch.json
   ```
   No necesitas editar nada — el `launch.json` ya apunta al `.env` que creaste.

---

## PASO 8 — Levantar el servidor

Presiona **`F5`** en VS Code o usa el **Spring Boot Dashboard** (ícono de hoja verde) → botón ▶ junto a `splitsnap-backend`.

Cuando veas esto, el servidor está listo:
```
Started SplitSnapApplication in X.XXX seconds
Tomcat started on port 8080
```

El navegador se abre automáticamente en Swagger. Si no ocurre, entra manualmente a:
```
http://localhost:8080/swagger-ui.html
```

**La primera vez que arranca:**
- Hibernate crea automáticamente las tablas en `splitsnap_db`
- Se insertan datos de prueba (3 usuarios y 2 grupos)

---

## PASO 9 — Verificar datos en DBeaver

Después de levantar el servidor por primera vez, abre DBeaver y navega a:

```
splitsnap_db → Tables → users
```

Clic derecho → **View Data** y deberías ver:

| name | email | phone |
|------|-------|-------|
| Carlos Huane | carlos@splitsnap.com | 987654321 |
| Ana Torres | ana@splitsnap.com | 912345678 |
| Juan Paredes | juan@splitsnap.com | 923456789 |

También revisa `groups` (2 grupos) y `group_members` (5 registros).

---

## PASO 10 — Probar los endpoints con Swagger

Para probar el login con un usuario de prueba:

1. Clic en `POST /api/auth/login` → **Try it out**
2. Pega este JSON:
```json
{
  "email": "carlos@splitsnap.com",
  "password": "test123"
}
```
3. Clic en **Execute** → debes recibir un `200 OK` con un token JWT
4. Copia el token, clic en **Authorize** (candado arriba a la derecha) y pégalo como `Bearer <token>`

---

## Datos de prueba

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

## Problemas frecuentes

| Error | Causa | Solución |
|-------|-------|----------|
| `mvn` no reconocido | Maven no está en el PATH | Repite el Paso 2 y reinicia CMD |
| `Could not resolve placeholder 'DB_PASSWORD'` | El `.env` no existe o está mal copiado | Verifica que creaste `.env` desde `.env.example` |
| `Communications link failure` | MySQL no está corriendo | Abre "Servicios" en Windows y arranca el servicio MySQL |
| `Access denied for user 'root'` | Password incorrecto en `.env` | Revisa que `DB_PASSWORD` coincide con tu instalación de MySQL |
| Puerto 8080 en uso | Otra app usa ese puerto | Cierra la otra app o cambia `server.port` en `application.properties` |
| Tablas no aparecen en DBeaver | Backend no arrancó aún | Levanta el backend primero, luego refresca DBeaver (`F5`) |

---

*SplitSnap Backend · Curso Herramientas de Desarrollo · 2026*
