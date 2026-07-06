@echo off
echo.
echo =========================================
echo Developer Workflow Automation Hub
echo =========================================
echo.

echo Checking Python installation...
python --version >nul 2>&1
if errorlevel 1 (
    echo ERROR: Python is not installed or not in PATH
    pause
    exit /b 1
)

echo Python found!
echo.

echo Installing dependencies...
cd backend
pip install -r requirements.txt

if errorlevel 1 (
    echo ERROR: Failed to install dependencies
    pause
    exit /b 1
)

echo.
echo =========================================
echo Starting Developer Workflow Automation Hub
echo =========================================
echo.
echo Backend Server: http://localhost:5000
echo API Documentation: http://localhost:5000/api/tasks
echo Frontend: Open frontend/index.html in your browser
echo.
echo Press Ctrl+C to stop the server
echo.

python app.py

pause
