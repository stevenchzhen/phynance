# Phynance: Loveable + Cursor Development Workflow

## Overview

This document outlines how to effectively use **Loveable** for UI/frontend design and **Cursor** for detailed coding and model development.

## Tool Responsibilities

### 🎨 **Loveable** (UI & General Structure)

- **Frontend Design**: React components, layouts, styling
- **User Experience**: Forms, dashboards, charts
- **Rapid Prototyping**: Quick UI iterations
- **Component Library**: Reusable UI components
- **Mock Data Integration**: Frontend development with sample data

### 💻 **Cursor** (Deep Development)

- **Backend Development**: Spring Boot APIs, services
- **Physics Models**: Mathematical calculations, algorithms
- **Data Processing**: Market data fetching, validation
- **Model Training**: ML model development and optimization
- **Performance Optimization**: Code efficiency, caching
- **Testing**: Unit tests, integration tests

## Project Structure

```
phynance/
├── backend-spring/              # 💻 CURSOR: Spring Boot backend
│   ├── src/main/java/...       # Physics models, APIs
│   └── pom.xml
├── frontend/                    # 💻 CURSOR: Production frontend
│   ├── src/                    # Optimized React app
│   └── package.json
├── frontend-loveable/           # 🎨 LOVEABLE: UI development
│   ├── components/             # UI components
│   ├── pages/                  # Page layouts
│   ├── assets/                 # Images, styles
│   └── mock-data/              # Sample data for development
├── integration/
│   ├── api-contracts/          # API specifications
│   └── shared-types/           # TypeScript definitions
└── docs/workflows/             # This file
```

## Development Workflow

### Phase 1: UI Design with Loveable

1. **Design Components** in Loveable:

   - Dashboard layout
   - Stock analysis forms
   - Chart components
   - Navigation menus

2. **Export/Generate Code**:

   - Download React components from Loveable
   - Place in `frontend-loveable/`

3. **Document APIs Needed**:
   - List required backend endpoints
   - Define data structures
   - Create mock responses

### Phase 2: Backend Development with Cursor

1. **Implement APIs** in Cursor:

   - Review UI requirements from Loveable
   - Build Spring Boot endpoints
   - Implement physics models

2. **Test with Mock Data**:
   - Verify API responses match UI expectations
   - Test error handling

### Phase 3: Integration

1. **Connect Frontend to Backend**:

   - Replace mock data with real API calls
   - Handle loading states and errors
   - Optimize performance

2. **Refine in Both Tools**:
   - UI adjustments in Loveable
   - Backend optimizations in Cursor

## API Integration Strategy

### Backend APIs (Cursor Development)

Base URL: `http://localhost:8081`

**Available Endpoints:**

- `GET /api/v1/viewer/health` - Health check
- `GET /api/v1/viewer/market-summary/{symbol}` - Market data
- `POST /api/v1/analysis/harmonic-oscillator` - Physics analysis
- `POST /api/v1/analysis/wave-physics` - Wave analysis
- `POST /api/v1/analysis/thermodynamics` - Thermal analysis

### Frontend Integration (Loveable to Production)

1. **Copy Components** from `frontend-loveable/` to `frontend/src/components/`
2. **Update API Calls** to use real backend endpoints
3. **Add Error Handling** and loading states
4. **Style Integration** with existing theme

## Git Workflow

### Branch Strategy

```bash
# Main development
main                    # Production-ready code

# Feature branches
feature/loveable-ui     # UI work from Loveable
feature/cursor-models   # Backend/model work from Cursor
feature/integration     # Combining both
```

### Commit Guidelines

- **Loveable commits**: `[UI] Add dashboard components`
- **Cursor commits**: `[Backend] Implement wave physics model`
- **Integration commits**: `[Integration] Connect UI to physics APIs`

## Best Practices

### 🎨 **When using Loveable:**

- Focus on user experience and visual design
- Use placeholder data for development
- Export clean, well-structured components
- Document required API endpoints

### 💻 **When using Cursor:**

- Implement robust backend logic
- Write comprehensive tests
- Optimize for performance
- Handle edge cases and errors

### 🔄 **Integration:**

- Keep API contracts updated
- Test thoroughly after each integration
- Maintain consistent data formats
- Document any breaking changes

## Example Workflow

### Day 1-2: Design Phase (Loveable)

```bash
# In Loveable:
# 1. Create stock analysis dashboard
# 2. Design physics model selection forms
# 3. Create chart visualization components
# 4. Export to frontend-loveable/
```

### Day 3-4: Backend Phase (Cursor)

```bash
# In Cursor:
cd phynance/backend-spring
# 1. Review UI requirements
# 2. Implement missing APIs
# 3. Optimize physics calculations
# 4. Add comprehensive testing
```

### Day 5: Integration Phase (Both)

```bash
# In Cursor:
cd phynance/frontend
# 1. Copy components from frontend-loveable/
# 2. Connect to real APIs
# 3. Test end-to-end functionality

# Back to Loveable if UI adjustments needed
# Then back to Cursor for final optimization
```

## Tools and Commands

### Starting Backend (Cursor)

```bash
cd phynance/backend-spring
mvn spring-boot:run
# Backend runs on http://localhost:8081
```

### Starting Frontend (Development)

```bash
cd phynance/frontend
npm start
# Frontend runs on http://localhost:3000
```

### Testing Integration

```bash
# Test API connectivity
curl http://localhost:8081/api/v1/viewer/health

# Test physics analysis
curl -X POST http://localhost:8081/api/v1/analysis/harmonic-oscillator \
  -H "Content-Type: application/json" \
  -d '{"symbol": "AAPL"}'
```

## Quick Start Commands

### Set up development environment:

```bash
# Start backend in one terminal
cd phynance/backend-spring && mvn spring-boot:run

# Start frontend in another terminal
cd phynance/frontend && npm start

# Open Loveable for UI design work
# Use this repo's frontend-loveable/ for exports
```

This workflow maximizes the strengths of both tools while maintaining a clean, integrated development process.
