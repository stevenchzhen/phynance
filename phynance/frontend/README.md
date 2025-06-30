# Phynance Financial Analytics Dashboard

This is a React + TypeScript frontend for the Phynance financial analytics dashboard, connecting to a Spring Boot backend.

## Tech Stack

- React + TypeScript
- Material-UI (MUI)
- React Query
- Chart.js (via react-chartjs-2)
- React Router
- Axios
- React Hook Form

## Getting Started

1. Install dependencies:
   ```sh
   npm install
   # or
   yarn install
   ```
2. Start the development server:
   ```sh
   npm start
   # or
   yarn start
   ```
3. The app will be available at `http://localhost:3000`.

## Project Structure

- `src/components/` — Reusable UI components (charts, tables, forms)
- `src/pages/` — Route-based pages (Dashboard, Users, Analytics, etc.)
- `src/api/` — API clients and hooks (using Axios and React Query)
- `src/types/` — TypeScript interfaces and DTOs
- `src/routes/` — React Router route definitions
- `src/hooks/` — Custom React hooks
- `src/theme/` — MUI theme customization
- `src/utils/` — Utility functions

## API

- Connects to the Spring Boot backend at `http://localhost:8080/api` by default.
