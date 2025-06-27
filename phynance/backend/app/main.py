from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import text
from sqlalchemy.orm import Session
import os
from datetime import datetime
from typing import Dict, Any

# Import database configuration
from app.database import get_db, engine, DATABASE_URL

# Create FastAPI app instance
app = FastAPI(
    title="Phynance API",
    description="A modern financial application API",
    version="1.0.0",
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=["http://localhost:3000"],  # React dev server
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Health check endpoint
@app.get("/")
async def root():
    return {
        "message": "Welcome to Phynance API",
        "version": "1.0.0",
        "status": "running"
    }

@app.get("/health")
async def health_check():
    """Health check endpoint to verify API status"""
    return {
        "status": "healthy",
        "service": "phynance-api",
        "timestamp": datetime.utcnow().isoformat(),
        "version": "1.0.0"
    }

@app.get("/health/db")
async def database_health_check(db: Session = Depends(get_db)):
    """Database health check endpoint"""
    try:
        # Test database connection
        result = db.execute(text("SELECT 1"))
        result.fetchone()
        return {
            "status": "healthy",
            "database": "connected",
            "timestamp": datetime.utcnow().isoformat()
        }
    except Exception as e:
        raise HTTPException(status_code=500, detail=f"Database connection failed: {str(e)}")

@app.get("/api/v1/status")
async def api_status():
    """API status endpoint with detailed information"""
    return {
        "api": {
            "name": "Phynance API",
            "version": "1.0.0",
            "status": "operational"
        },
        "database": {
            "url": DATABASE_URL.split("://")[0] if "://" in DATABASE_URL else "sqlite",
            "status": "configured"
        },
        "timestamp": datetime.utcnow().isoformat()
    }

# Include API routers here
# from app.api import users, transactions, etc.

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=True,
        log_level="info"
    ) 