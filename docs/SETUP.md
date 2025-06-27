# Setup Guide

## Quick Start

### 1. Activate Virtual Environment

**Windows:**
```bash
# Option 1: Use the batch file
activate_venv.bat

# Option 2: Manual activation
venv\Scripts\activate
```

**macOS/Linux:**
```bash
source venv/bin/activate
```

### 2. Install Backend Dependencies

```bash
cd phynance/backend
pip install -r requirements.txt
```

### 3. Install Frontend Dependencies

```bash
cd phynance/frontend
npm install
```

### 4. Run the Application

**Backend (Terminal 1):**
```bash
cd phynance/backend
python app/main.py
```

**Frontend (Terminal 2):**
```bash
cd phynance/frontend
npm start
```

## Development Workflow

1. **Backend Development:**
   - API endpoints: `phynance/backend/app/api/`
   - Database models: `phynance/backend/app/models/`
   - Tests: `phynance/backend/tests/`

2. **Frontend Development:**
   - React components: `phynance/frontend/src/components/`
   - Static assets: `phynance/frontend/public/`

3. **Testing:**
   - Backend tests: `pytest` (from backend directory)
   - Frontend tests: `npm test` (from frontend directory)

## API Documentation

Once the backend is running, visit:
- Swagger UI: `http://localhost:8000/docs`
- ReDoc: `http://localhost:8000/redoc`

## Environment Variables

Create a `.env` file in the backend directory for environment-specific configuration:

```env
DATABASE_URL=sqlite:///./phynance.db
SECRET_KEY=your-secret-key-here
DEBUG=True
``` 