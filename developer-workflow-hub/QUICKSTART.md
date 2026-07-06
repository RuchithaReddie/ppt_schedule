# Developer Workflow Automation Hub - Quick Start Guide

## 🚀 Installation & Running

### Step 1: Install Python Dependencies
```bash
cd backend
pip install -r requirements.txt
```

### Step 2: Start the Flask Backend Server
```bash
python app.py
```

You should see:
```
========================================
Developer Workflow Automation Hub
========================================

Starting server on http://localhost:5000
API Documentation: http://localhost:5000/api/tasks
Frontend: http://localhost:5000/static/index.html

========================================
```

### Step 3: Open the Frontend
Simply open `frontend/index.html` in your web browser, or use a local HTTP server:

```bash
# Option 1: Python HTTP Server (in frontend directory)
cd frontend
python -m http.server 8000
# Then visit http://localhost:8000
```

### Step 4: Start Using the Application
Navigate through the tabs:
1. **Overview** - Learn about the application
2. **Repository Analysis** - Analyze GitHub repos
3. **Documentation** - Generate project documentation
4. **Code Quality** - Upload and analyze code files
5. **Full Workflow** - Execute complete automation
6. **Reports** - Download generated reports

---

## 🧪 Test Cases

### Test 1: Repository Analysis
1. Go to "Repository Analysis" tab
2. Enter: `https://github.com/torvalds/linux`
3. Click "Analyze Repository"
4. View statistics and download report

### Test 2: Documentation Generation
1. Go to "Documentation" tab
2. Enter: `https://github.com/facebook/react`
3. Click "Generate Documentation"
4. View features and technologies

### Test 3: Code Quality Analysis
1. Create or find a small Java/Python file
2. Go to "Code Quality" tab
3. Upload the file
4. Review quality score and recommendations

### Test 4: Full Workflow
1. Go to "Full Workflow" tab
2. Enter: `https://github.com/microsoft/vscode`
3. Click "Execute Workflow"
4. Wait for analysis and documentation to complete

---

## 📁 Project Files Explained

| File | Purpose |
|------|---------|
| `app.py` | Main Flask application & API endpoints |
| `config.py` | Configuration and settings |
| `tasks/base_task.py` | Abstract base class for all tasks |
| `tasks/repository_analysis_task.py` | Repository data fetcher |
| `tasks/documentation_task.py` | Documentation generator |
| `tasks/code_quality_task.py` | Code analysis implementation |
| `services/workflow_service.py` | Task orchestration |
| `services/report_service.py` | Report generation & storage |
| `controllers/task_controller.py` | API request handler |
| `utils/github_client.py` | GitHub API integration |
| `frontend/index.html` | Web interface |
| `frontend/style.css` | Styling |
| `frontend/script.js` | Frontend logic |

---

## 🔧 Troubleshooting

### Issue: "ModuleNotFoundError: No module named 'flask'"
**Solution**: Make sure you installed requirements.txt
```bash
pip install -r requirements.txt
```

### Issue: Port 5000 already in use
**Solution**: Change port in app.py (line ~275)
```python
app.run(debug=True, host="0.0.0.0", port=5001)  # Change 5000 to 5001
```

### Issue: GitHub API rate limiting
**Solution**: Get a personal GitHub token and set it in environment:
```bash
set GITHUB_TOKEN=your_token_here  # Windows
export GITHUB_TOKEN=your_token_here  # Mac/Linux
```

### Issue: CORS errors
**Solution**: The Flask app has CORS enabled. If still having issues, verify:
1. Backend is running on http://localhost:5000
2. Frontend can access http://localhost:5000/api/tasks

---

## 📊 API Endpoints Summary

| Endpoint | Method | Purpose |
|----------|--------|---------|
| `/` | GET | Health check |
| `/api/tasks` | GET | List available tasks |
| `/api/tasks/repository-analysis` | POST | Analyze repository |
| `/api/tasks/documentation` | POST | Generate documentation |
| `/api/tasks/code-quality` | POST | Analyze code quality |
| `/api/workflow/execute` | POST | Execute full workflow |
| `/api/reports` | GET | List all reports |
| `/api/reports/download/<filename>` | GET | Download specific report |

---

## 💻 Command Line Examples

### Example 1: Quick Repository Analysis
```bash
curl -X POST http://localhost:5000/api/tasks/repository-analysis \
  -H "Content-Type: application/json" \
  -d '{"repo_url": "https://github.com/python/cpython"}'
```

### Example 2: Test API Health
```bash
curl http://localhost:5000/
```

### Example 3: List Available Tasks
```bash
curl http://localhost:5000/api/tasks
```

---

## 📈 Performance Expectations

- **Repository Analysis**: 1-3 seconds (depends on GitHub API)
- **Documentation Generation**: 1-2 seconds
- **Code Quality Analysis**: <1 second
- **Full Workflow**: 3-5 seconds

---

## 🎯 Key Features

✅ **Modular Architecture** - Easy to extend with new tasks  
✅ **SOLID Principles** - Clean, maintainable code  
✅ **REST API** - Standard interface for automation  
✅ **Report Generation** - Professional output documents  
✅ **GitHub Integration** - Real repository analysis  
✅ **Responsive UI** - Works on desktop and tablet  
✅ **Error Handling** - Graceful error messages  

---

## 📝 Next Steps

1. Explore the code structure
2. Try each task individually
3. Create custom tasks by extending `BaseTask`
4. Add new API endpoints
5. Deploy to a cloud platform

Enjoy automating your developer workflow! 🚀
