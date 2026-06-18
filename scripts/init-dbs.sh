#!/bin/bash
# Creates all 7 databases for the Report System
set -e

DB_USER="${DB_USER:-report_user}"
DB_PASSWORD="${DB_PASSWORD:-report_pass}"
DB_HOST="${DB_HOST:-localhost}"
DB_PORT="${DB_PORT:-5432}"

DATABASES=(
  "auth_db"
  "property_db"
  "restaurant_db"
  "inventory_db"
  "finance_db"
  "payment_db"
  "reporting_db"
)

echo "Creating databases..."
for db in "${DATABASES[@]}"; do
  echo "  Creating $db..."
  PGPASSWORD=$DB_PASSWORD createdb -h $DB_HOST -p $DB_PORT -U $DB_USER $db 2>/dev/null || echo "  $db already exists"
done

echo "All databases created successfully."
