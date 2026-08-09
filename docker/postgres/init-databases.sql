-- NightFlow - PostgreSQL bootstrap for the Docker Compose stack.
--
-- The postgres image creates POSTGRES_DB (nightflow_auth) itself; the remaining
-- per-service databases are created here. This script only runs when the data
-- volume is empty, i.e. on a fresh `docker compose up` (or after `down -v`).

CREATE DATABASE nightflow_venue;
CREATE DATABASE nightflow_ticket;
CREATE DATABASE nightflow_order;
