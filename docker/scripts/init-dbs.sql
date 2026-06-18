-- init-dbs.sql — Create all service databases on first Postgres startup
-- Runs automatically via /docker-entrypoint-initdb.d/ on first init of the data dir.

CREATE DATABASE auth_db;
CREATE DATABASE property_db;
CREATE DATABASE restaurant_db;
CREATE DATABASE inventory_db;
CREATE DATABASE finance_db;
CREATE DATABASE payment_db;
CREATE DATABASE reporting_db;
