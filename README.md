# Phynance

A modern financial application built with Python backend and React frontend.

## Project Structure

```
phynance/
├── backend/
│   ├── app/
│   │   ├── models/
│   │   └── api/
│   └── tests/
├── frontend/
│   ├── src/
│   │   └── components/
│   └── public/
└── docs/
```

## Setup Instructions

### Prerequisites

- Python 3.9+
- Node.js (for React frontend)
- Git

### Backend Setup

1. Activate the virtual environment:
   ```bash
   # Windows
   venv\Scripts\activate
   
   # macOS/Linux
   source venv/bin/activate
   ```

2. Install Python dependencies:
   ```bash
   cd phynance/backend
   pip install -r requirements.txt
   ```

3. Run the backend server:
   ```bash
   python app/main.py
   ```

### Frontend Setup

1. Navigate to the frontend directory:
   ```bash
   cd phynance/frontend
   ```

2. Install Node.js dependencies:
   ```bash
   npm install
   ```

3. Start the development server:
   ```bash
   npm start
   ```

## Development

- Backend API runs on: `http://localhost:8000`
- Frontend development server runs on: `http://localhost:3000`

## Contributing

1. Fork the repository
2. Create a feature branch
3. Make your changes
4. Add tests
5. Submit a pull request
