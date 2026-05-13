@echo off
cd /d "%~dp0" 2>nul || (echo ERROR: No se pudo acceder a la carpeta del proyecto. && echo Ejecuta este archivo desde CMD, no con doble clic. && pause && exit /b 1)
echo ================================
echo   SplitSnap Backend - Inicio
echo ================================

set DB_URL=jdbc:postgresql://localhost:5432/splitsnap_db
set DB_USERNAME=postgres
set DB_PASSWORD=CAMBIA_ESTO
set JWT_SECRET=splitsnap_clave_secreta_2026_desarrollo

echo [1/3] Compilando proyecto...
mvn package -DskipTests
if %errorlevel% neq 0 (
    echo ERROR: Fallo la compilacion. Revisa los errores arriba.
    pause
    exit /b 1
)

echo [2/3] Copiando JAR...
copy target\splitsnap-backend-0.0.1-SNAPSHOT.jar %USERPROFILE%\Desktop\splitsnap.jar

echo [3/3] Iniciando servidor en http://localhost:8080
echo Swagger UI: http://localhost:8080/swagger-ui.html
echo Presiona Ctrl+C para detener el servidor.
echo.
java -jar %USERPROFILE%\Desktop\splitsnap.jar
pause
