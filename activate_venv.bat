@echo off
echo Activating Python virtual environment...
call venv\Scripts\activate.bat
echo Virtual environment activated!
echo.
echo To deactivate, run: deactivate
echo To install backend dependencies: cd phynance/backend && pip install -r requirements.txt
echo.
cmd /k 