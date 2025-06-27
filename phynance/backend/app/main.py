from fastapi import FastAPI, Depends, HTTPException
from fastapi.middleware.cors import CORSMiddleware
from sqlalchemy import text
from sqlalchemy.orm import Session
from datetime import datetime
from typing import Dict, Any

# Import database configuration and settings
from app.database import get_db, engine, DATABASE_URL
from app.config import settings

# Create FastAPI app instance
app = FastAPI(
    title=settings.app_name,
    description="A modern financial application API",
    version=settings.app_version,
    docs_url="/docs",
    redoc_url="/redoc"
)

# Configure CORS
app.add_middleware(
    CORSMiddleware,
    allow_origins=settings.allowed_origins,
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

# Health check endpoint
@app.get("/")
async def root():
    return {
        "message": f"Welcome to {settings.app_name}",
        "version": settings.app_version,
        "status": "running",
        "environment": settings.environment
    }

@app.get("/health")
async def health_check():
    """Health check endpoint to verify API status"""
    return {
        "status": "healthy",
        "service": settings.app_name,
        "timestamp": datetime.utcnow().isoformat(),
        "version": settings.app_version,
        "environment": settings.environment
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
            "name": settings.app_name,
            "version": settings.app_version,
            "status": "operational",
            "environment": settings.environment
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
        reload=settings.debug,
        log_level=settings.log_level.lower()
    ) 