#!/usr/bin/env python3
"""
Startup script for Phynance API
"""
import uvicorn
from app.config import settings

if __name__ == "__main__":
    print(f"Starting {settings.app_name} v{settings.app_version}")
    print(f"Environment: {settings.environment}")
    print(f"Debug mode: {settings.debug}")
    print(f"Database: {settings.database_url.split('://')[0] if '://' in settings.database_url else 'sqlite'}")
    print("-" * 50)
    
    uvicorn.run(
        "app.main:app",
        host="0.0.0.0",
        port=8000,
        reload=settings.debug,
        log_level=settings.log_level.lower()
    ) 