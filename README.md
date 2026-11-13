# Hospital Management System

[![CI](https://github.com/Sanjay-SRH/hms/actions/workflows/ci.yml/badge.svg)](https://github.com/Sanjay-SRH/hms/actions/workflows/ci.yml)

Full-stack hospital management system: patient registration, appointments,
consultation, billing, pharmacy and inpatient management.

## Tech stack
- Backend: Java 21, Spring Boot 4, Spring Security, Spring Data JPA, Flyway
- Databases: PostgreSQL (transactional data), MongoDB (audit log, clinical notes)
- Frontend: React, TypeScript, Vite, Tailwind CSS
- DevOps: Docker Compose, GitHub Actions

## Run locally
1. Start the databases: `docker compose up -d`
2. Backend: `cd backend/hms-backend` then `./mvnw spring-boot:run`
   Health check: http://localhost:8080/actuator/health
3. Frontend: `cd frontend`, `npm install`, `npm run dev`
   Open http://localhost:5173

## Status
Stage 0 (project setup) in progress. Next: authentication, roles, audit log.
