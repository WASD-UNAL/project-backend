@echo off
setlocal EnableDelayedExpansion

REM =============================================
REM  Cargar variables desde .env
REM =============================================
if not exist ".env" (
    echo No se encontro el archivo .env en la raiz.
    exit /b 1
)

echo Cargando variables desde .env...
for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    set "line=%%A"
    REM Ignorar comentarios y lineas vacias
    if not "!line!"=="" (
        echo !line! | findstr /r "^[^#]" >nul 2>&1
        if !errorlevel!==0 (
            set "%%A=%%B"
        )
    )
)

REM =============================================
REM  Verificar infraestructura Docker
REM =============================================
echo ^>^>^>Verificando infraestructura...

docker ps -q -f "name=project_db" > "%TEMP%\docker_check.txt" 2>&1
set /p CONTAINER_ID=<"%TEMP%\docker_check.txt"

if defined CONTAINER_ID (
    echo La base de datos ya esta en ejecucion.
) else (
    echo Iniciando contenedor de base de datos...
    docker-compose up -d
    if !errorlevel! neq 0 (
        echo Fallo docker-compose
        exit /b 1
    )
    echo Esperando a que PostgreSQL este listo...
    set "MAX_TRIES=20"
    set "TRY=0"
    :wait_loop
        docker exec "%DB_NAME%" pg_isready -U "%DB_USER%" >nul 2>&1
        if !errorlevel!==0 goto db_ready
        set /a TRY+=1
        if !TRY! geq !MAX_TRIES! (
            echo ERROR: PostgreSQL no respondio despues de !MAX_TRIES! intentos.
            exit /b 1
        )
        timeout /t 3 /nobreak >nul
        goto wait_loop
    :db_ready
    echo PostgreSQL listo.
)

REM =============================================
REM  Inicializar base de datos
REM =============================================
if exist "scripts\init.sql" (
    echo Iniciando la Base de datos...
    docker exec -i "%DB_NAME%" psql -U "%DB_USER%" -d "%DB_NAME%" < scripts\init.sql
    if !errorlevel!==0 (
        echo Base de datos inicializada con exito.
    ) else (
        echo Fallo la ejecucion del SQL inicial.
        exit /b 1
    )
) else (
    echo No se encontro scripts\init.sql, saltando inicializacion.
)

REM =============================================
REM  Compilar con Gradle
REM =============================================
echo ^>^>^>Instalando dependencias y compilando...
call gradlew.bat clean build -x test
if !errorlevel! neq 0 (
    echo ERROR: Fallo la compilacion de Gradle
    exit /b 1
)

REM =============================================
REM  Levantar aplicacion
REM =============================================
echo ^>^>^>Levantando repositorio en http://localhost:8080
call gradlew.bat bootRun

endlocal