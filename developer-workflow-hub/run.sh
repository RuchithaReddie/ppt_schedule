#!/bin/bash

echo ""
echo "========================================="
echo "Developer Workflow Automation Hub"
echo "========================================="
echo ""

echo "Checking Python installation..."
if ! command -v python3 &> /dev/null; then
    echo "ERROR: Python 3 is not installed"
    exit 1
fi

echo "Python found!"
echo ""

echo "Installing dependencies..."
cd backend
pip install -r requirements.txt

if [ $? -ne 0 ]; then
    echo "ERROR: Failed to install dependencies"
    exit 1
fi

echo ""
echo "========================================="
echo "Starting Developer Workflow Automation Hub"
echo "========================================="
echo ""
echo "Backend Server: http://localhost:5000"
echo "API Documentation: http://localhost:5000/api/tasks"
echo "Frontend: Open frontend/index.html in your browser"
echo ""
echo "Press Ctrl+C to stop the server"
echo ""

python3 app.py
