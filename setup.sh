#!/bin/bash

# --- Bloque 1: Carga de variables de entorno ---
if [ -f .env ]; then
  echo "🕐 Cargando variables desde .env..."
  set -a
  source .env
  set +a
else
  echo "❌ ERROR: No se encontró el archivo .env en la raíz."
  exit 1
fi

# --- Bloque 2: Infraestructura (Docker) ---
echo ">>> [1/4] Verificando infraestructura..."
if [ "$(docker ps -q -f name=project_db)" ]; then
  echo "✅ La base de datos ya está en ejecución."
else
  echo "🐘 Iniciando contenedor de base de datos..."
  docker-compose up -d || { echo "❌ ERROR: Falló docker-compose"; exit 1; }
  echo "⏳ Esperando 5s para que el motor de base de datos arranque..."
  sleep 5
fi

# --- Bloque 3: Inicialización de Datos (SQL) ---
if [ -f scripts/init.sql ]; then
  echo "💉 Iniciando la Base de datos..."
  # Se usan las variables cargadas del .env automáticamente
  docker exec -i project_db psql -U "$DB_USER" -d "$DB_NAME" < scripts/init.sql
  if [ $? -eq 0 ]; then
    echo "✅ Base de datos inicializada con éxito."
  else
    echo "❌ ERROR: Falló la ejecución del SQL inicial."
    exit 1
  fi
else
  echo "⚠️ AVISO: No se encontró scripts/init.sql, saltando inicialización."
fi

# --- Bloque 4: Compilación y Ejecución ---
echo ">>> [3/4] Instalando dependencias y compilando..."
chmod +x gradlew
./gradlew clean build -x test || { echo "❌ ERROR: Falló la compilación de Gradle"; exit 1; }

echo ">>> [4/4] Levantando repositorio en http://localhost:8080"
./gradlew bootRun