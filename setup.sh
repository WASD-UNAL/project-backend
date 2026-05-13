#!/bin/bash

if [ -f .env ]; then
  echo "Cargando variables desde .env..."
  set -a
  source .env
  set +a
else
  echo "No se encontró el archivo .env en la raíz."
  exit 1
fi

echo ">>>Verificando infraestructura..."
if [ "$(docker ps -q -f name=project_db)" ]; then
  echo "La base de datos ya está en ejecución."
else
  echo "Iniciando contenedor de base de datos..."
  docker-compose up -d || { echo "Falló docker-compose"; exit 1; }

  echo "Esperando a que PostgreSQL esté listo..."
  MAX_TRIES=20
  TRY=0
  until docker exec "$DB_NAME" pg_isready -U "$DB_USER" > /dev/null 2>&1; do
    TRY=$((TRY + 1))
    if [ "$TRY" -ge "$MAX_TRIES" ]; then
      echo "ERROR: PostgreSQL no respondió"
      exit 1
    fi
    sleep 3
  done
  echo "PostgreSQL listo."
fi

if [ -f scripts/init.sql ]; then
  echo "Iniciando la Base de datos..."
  docker exec -i "$DB_NAME" psql -U "$DB_USER" -d "$DB_NAME" < scripts/init.sql
  if [ $? -eq 0 ]; then
    echo "Base de datos inicializada con éxito."
  else
    echo "Falló la ejecución del SQL inicial."
    exit 1
  fi
else
  echo "No se encontró scripts/init.sql, saltando inicialización."
fi

echo ">>>Instalando dependencias y compilando..."
chmod +x gradlew
./gradlew clean build -x test || { echo "❌ ERROR: Falló la compilación de Gradle"; exit 1; }

echo ">>>Levantando repositorio en http://localhost:8080"
./gradlew bootRun