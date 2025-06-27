from pydantic_settings import BaseSettings
from typing import List, Optional
import os

class Settings(BaseSettings):
    # Database
    database_url: str = "sqlite:///./phynance.db"
    
    # Application
    app_name: str = "Phynance API"
    app_version: str = "1.0.0"
    debug: bool = True
    environment: str = "development"
    
    # Security
    secret_key: str = "your-super-secret-key-change-this-in-production"
    algorithm: str = "HS256"
    access_token_expire_minutes: int = 30
    
    # CORS
    allowed_origins: List[str] = ["http://localhost:3000", "http://127.0.0.1:3000"]
    
    # API
    api_v1_str: str = "/api/v1"
    project_name: str = "Phynance"
    
    # Logging
    log_level: str = "INFO"
    
    class Config:
        env_file = ".env"
        env_file_encoding = "utf-8"
        case_sensitive = False

# Create settings instance
settings = Settings()

# Database URL with fallback
def get_database_url() -> str:
    """Get database URL with environment variable override"""
    return os.getenv("DATABASE_URL", settings.database_url) 