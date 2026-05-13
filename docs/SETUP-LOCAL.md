# Guía de Setup Local — SplitSnap Backend
> Sigue esta guía en orden. No saltes pasos.

---

## PASO 1 — Instalar Java 17+

1. Descarga **Eclipse Temurin JDK 21** desde: https://adoptium.net
2. Ejecuta el instalador `.msi` y sigue los pasos (todo por defecto)
3. Verifica en CMD:
```
java -version
```
Debe mostrar `openjdk version "21.x.x"` o similar.

---

## PASO 2 — Instalar Maven

1. Descarga **Apache Maven 3.9+** desde: https://maven.apache.org/download.cgi
   → Elige **Binary zip archive** (el archivo `.zip`, no el source)

2. Descomprime el zip. Ejemplo: queda en `C:\Users\TU_USUARIO\Downloads\apache-maven-3.9.15-bin\apache-maven-3.9.15`

3. Agrega Maven al PATH:
   - Busca **"Variables de entorno"** en el menú inicio
   - Abre **"Editar las variables de entorno del sistema"**
   - Clic en **"Variables de entorno..."**
   - En **"Variables del sistema"** → busca `Path` → doble clic
   - Clic en **"Nuevo"** → pega la ruta completa a la carpeta `bin` de Maven:
     ```
     C:\Users\TU_USUARIO\Downloads\apache-maven-3.9.15-bin\apache-maven-3.9.15\bin
     ```
   - Acepta en todas las ventanas

4. **Cierra y vuelve a abrir CMD** (importante)

5. Verifica:
```
mvn --version
```
Debe mostrar `Apache Maven 3.9.x`

---

## PASO 3 — Instalar PostgreSQL

1. Descarga **PostgreSQL 17** desde: https://www.postgresql.org/download/windows
2. Ejecuta el instalador
3. Durante la instalación te pedirá una contraseña para el usuario `postgres` → **anótala**, la necesitarás después
4. Puerto por defecto: `5432` → déjalo así
5. Al finalizar, desmarca la opción de "Stack Builder" y termina

---

## PASO 4 — Si olvidaste la contraseña de PostgreSQL

> Si recuerdas la contraseña, salta al Paso 5.

1. Abre **PowerShell como Administrador** (clic derecho → "Ejecutar como administrador")

2. Detén el servicio:
```
net stop postgresql-x64-17
```

3. Abre el archivo de configuración:
```
notepad "C:\Program Files\PostgreSQL\17\data\pg_hba.conf"
```

4. Busca las líneas con `scram-sha-256` y cámbialas a `trust`:
```
# Antes:
host    all             all             127.0.0.1/32            scram-sha-256
# Después:
host    all             all             127.0.0.1/32            trust
```
   Guarda el archivo.

5. Inicia el servicio:
```
net start postgresql-x64-17
```

6. Conéctate sin contraseña:
```
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres
```

7. Cambia la contraseña (reemplaza `nueva_password` por la que quieras):
```sql
ALTER USER postgres WITH PASSWORD 'nueva_password';
\q
```

8. Vuelve al `pg_hba.conf`, revierte `trust` a `scram-sha-256` y reinicia el servicio:
```
net stop postgresql-x64-17
net start postgresql-x64-17
```

---

## PASO 5 — Crear la base de datos

Abre **PowerShell** (normal, no administrador) y ejecuta:
```
& "C:\Program Files\PostgreSQL\17\bin\psql.exe" -U postgres
```
Te pedirá la contraseña. Una vez dentro:
```sql
CREATE DATABASE splitsnap_db;
\q
```

---

## PASO 6 — Clonar el repositorio

Abre **CMD** (no PowerShell, para evitar problemas de sintaxis) y ejecuta:
```
git clone https://github.com/Carlos-Huane/SplitSnap-BackEnd.git
```

Esto crea una carpeta `SplitSnap-BackEnd` donde ejecutaste el comando. Puedes clonar en cualquier carpeta de tu preferencia, por ejemplo en el escritorio o en `Documentos`.

> ⚠️ **Evita clonar en rutas con caracteres especiales** (emojis, tildes, paréntesis, espacios) — Java puede fallar al compilar o ejecutar el JAR desde esas rutas. Usa rutas simples como `C:\Users\TU_USUARIO\Desktop\SplitSnap-BackEnd`.

---

## PASO 7 — Configurar variables de entorno

En la misma ventana de CMD donde correrás el proyecto, ejecuta:
```
set DB_URL=jdbc:postgresql://localhost:5432/splitsnap_db
set DB_USERNAME=postgres
set DB_PASSWORD=tu_password_de_postgresql
set JWT_SECRET=splitsnap_clave_secreta_2026_desarrollo
```

> ⚠️ Estas variables solo duran mientras esa ventana de CMD esté abierta. Cada vez que abras CMD nuevo debes volver a ejecutarlas.

---

## PASO 8 — Compilar y ejecutar

> **Método recomendado: usar VS Code con Spring Boot Dashboard** (ver sección "Inicio rápido con VS Code" al final de este documento). Si usas ese método, salta al Paso 9 directamente.

El método por CMD es alternativo, por si no tienes VS Code o prefieres la terminal.

### 8.1 Compilar el proyecto
```
mvn package -DskipTests
```
La primera vez descarga dependencias (puede tardar 2-3 minutos). Al final debe decir `BUILD SUCCESS`.

### 8.2 Copiar el JAR a tu escritorio

> ⚠️ Si la ruta donde clonaste el proyecto contiene espacios, tildes, emojis u otros caracteres especiales, Java falla al ejecutar el JAR desde esa carpeta. La solución es copiarlo al escritorio antes de correrlo.
>
> Si tu ruta **no** tiene caracteres especiales, puedes saltarte este paso y ejecutar directamente desde `target/`.

```
copy target\splitsnap-backend-0.0.1-SNAPSHOT.jar %USERPROFILE%\Desktop\splitsnap.jar
```

### 8.3 Ejecutar
```
java -jar %USERPROFILE%\Desktop\splitsnap.jar
```

> Si no copiaste el JAR, ejecuta desde la carpeta del proyecto:
> ```
> java -jar target\splitsnap-backend-0.0.1-SNAPSHOT.jar
> ```

### 8.4 Verificar que arrancó
Cuando veas esto en la consola, el servidor está listo:
```
Started SplitSnapApplication in X.XXX seconds
Tomcat started on port 8080
```

---

## PASO 9 — Probar los endpoints

Abre el navegador y entra a:
```
http://localhost:8080/swagger-ui.html
```

Verás la documentación interactiva con todos los endpoints. Para probar:

1. Haz clic en `POST /api/auth/register`
2. Clic en **"Try it out"**
3. Pega este JSON en el body:
```json
{
  "name": "Carlos Huane",
  "email": "carlos@splitsnap.com",
  "phone": "987654321",
  "password": "123456"
}
```
4. Clic en **"Execute"**
5. Debes recibir un `201 Created` con un token JWT

---

## Inicio rápido con VS Code (recomendado)

Este es el método más fácil. No necesitas CMD ni variables de entorno manuales.

### Extensiones requeridas (instálalas una sola vez)

1. Abre VS Code
2. Ve a la pestaña de Extensiones (ícono de cuadraditos a la izquierda, o `Ctrl+Shift+X`)
3. Busca e instala:
   - **Extension Pack for Java** — de Microsoft
   - **Spring Boot Extension Pack** — de VMware

### Configurar tu launch.json

El archivo `.vscode/launch.json` está en `.gitignore` (cada uno tiene el suyo). Al clonar el repo, ese archivo no existe. Debes crearlo:

1. En la raíz del proyecto clonado, busca la carpeta `.vscode/`
2. Dentro hay un archivo `launch.json.example` — **cópialo** y renómbralo a `launch.json`
3. Abre `launch.json` y reemplaza `TU_PASSWORD_POSTGRES_AQUI` por tu contraseña de PostgreSQL

> ⚠️ No subas `launch.json` al repo — ya está en `.gitignore`.

### Levantar el servidor

Con las extensiones instaladas y el `launch.json` configurado:

- Opción A: Presiona `F5` desde VS Code
- Opción B: Abre la pestaña **Spring Boot Dashboard** (ícono de hoja verde en la barra lateral) → clic en el botón ▶ junto a `splitsnap-backend`

Cuando veas esto en la consola, el servidor está listo:
```
Started SplitSnapApplication in X.XXX seconds
Tomcat started on port 8080
```

---

## Problemas frecuentes

| Error | Causa | Solución |
|-------|-------|----------|
| `mvn` no reconocido | Maven no está en el PATH | Repite el Paso 2 y reinicia CMD |
| `falló la autentificación` en psql | Contraseña incorrecta | Sigue el Paso 4 para resetearla |
| `Error decoding percent encoded characters` | Ruta con emojis/caracteres especiales | Copia el JAR al escritorio (Paso 8.2) |
| Puerto 8080 en uso | Otra app usa ese puerto | Cierra la otra app o cambia el puerto en `application.properties` |
| `Connection refused` a PostgreSQL | Servicio detenido | Ejecuta `net start postgresql-x64-17` |

---

*SplitSnap Backend · Curso Herramientas de Desarrollo · 2026*
