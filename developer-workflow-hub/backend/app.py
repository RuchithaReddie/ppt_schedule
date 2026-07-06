from flask import Flask, request, jsonify, send_file, send_from_directory
from flask_cors import CORS
from pathlib import Path
import sys
import os

# Add backend to path
sys.path.insert(0, os.path.dirname(os.path.abspath(__file__)))

from config import Config
from controllers.task_controller import TaskController

app = Flask(__name__)
CORS(app)

# Initialize controller
task_controller = TaskController()

# =====================
# API Endpoints
# =====================

@app.route("/", methods=["GET"])
def index():
    """Health check endpoint"""
    return jsonify({
        "status": "online",
        "application": "Developer Workflow Automation Hub",
        "version": "1.0.0",
        "description": "Automate developer tasks: analyze repositories, generate documentation, and check code quality"
    }), 200


@app.route("/api/tasks", methods=["GET"])
def get_available_tasks():
    """Get list of available tasks"""
    tasks = [
        {
            "name": "RepositoryAnalysis",
            "description": "Analyze a GitHub repository",
            "endpoint": "/api/tasks/repository-analysis",
            "method": "POST",
            "inputs": {
                "repo_url": "GitHub repository URL (e.g., https://github.com/owner/repo)"
            }
        },
        {
            "name": "DocumentationGeneration",
            "description": "Generate documentation from repository data",
            "endpoint": "/api/tasks/documentation",
            "method": "POST",
            "inputs": {
                "repo_url": "GitHub repository URL"
            }
        },
        {
            "name": "CodeQualityAnalysis",
            "description": "Analyze code quality of uploaded file",
            "endpoint": "/api/tasks/code-quality",
            "method": "POST",
            "inputs": {
                "file": "Java/code file (multipart file upload)"
            }
        },
        {
            "name": "FullWorkflow",
            "description": "Execute complete workflow: analyze repo → generate documentation",
            "endpoint": "/api/workflow/execute",
            "method": "POST",
            "inputs": {
                "repo_url": "GitHub repository URL"
            }
        }
    ]
    
    return jsonify({
        "status": "success",
        "tasks": tasks,
        "total": len(tasks)
    }), 200


# =====================
# Repository Analysis
# =====================

@app.route("/api/tasks/repository-analysis", methods=["POST"])
def repository_analysis():
    """Execute repository analysis task"""
    try:
        data = request.get_json()
        repo_url = data.get("repo_url")
        
        if not repo_url:
            return jsonify({
                "status": "error",
                "error": "repo_url is required"
            }), 400
        
        result = task_controller.execute_repository_analysis(repo_url)
        status_code = 200 if result.get("status") == "success" else 400
        
        return jsonify(result), status_code
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


# =====================
# Documentation Generation
# =====================

@app.route("/api/tasks/documentation", methods=["POST"])
def documentation_generation():
    """Execute documentation generation task"""
    try:
        data = request.get_json()
        repo_url = data.get("repo_url")
        
        if not repo_url:
            return jsonify({
                "status": "error",
                "error": "repo_url is required"
            }), 400
        
        result = task_controller.execute_documentation_task(repo_url)
        status_code = 200 if result.get("status") == "success" else 400
        
        return jsonify(result), status_code
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


# =====================
# Code Quality Analysis
# =====================

@app.route("/api/tasks/code-quality", methods=["POST"])
def code_quality_analysis():
    """Execute code quality analysis task"""
    try:
        # Check if file is in request
        if 'file' not in request.files:
            return jsonify({
                "status": "error",
                "error": "file is required"
            }), 400
        
        file = request.files['file']
        
        if file.filename == '':
            return jsonify({
                "status": "error",
                "error": "No file selected"
            }), 400
        
        # Read file content
        file_content = file.read().decode('utf-8')
        
        result = task_controller.execute_code_quality_task(file_content, file.filename)
        status_code = 200 if result.get("status") == "success" else 400
        
        return jsonify(result), status_code
    
    except UnicodeDecodeError:
        return jsonify({
            "status": "error",
            "error": "File must be text-based (Java, Python, etc.)"
        }), 400
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


# =====================
# Workflow Execution
# =====================

@app.route("/api/workflow/execute", methods=["POST"])
def execute_workflow():
    """Execute complete workflow"""
    try:
        data = request.get_json()
        repo_url = data.get("repo_url")
        
        if not repo_url:
            return jsonify({
                "status": "error",
                "error": "repo_url is required"
            }), 400
        
        result = task_controller.execute_full_workflow(repo_url)
        status_code = 200 if result.get("status") == "completed" else 400
        
        return jsonify(result), status_code
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


# =====================
# Reports
# =====================

@app.route("/api/reports", methods=["GET"])
def list_reports():
    """List all generated reports"""
    try:
        reports = []
        for report_file in Config.REPORTS_DIR.glob("*.txt"):
            reports.append({
                "name": report_file.name,
                "path": f"/api/reports/download/{report_file.name}",
                "created": report_file.stat().st_mtime
            })
        
        return jsonify({
            "status": "success",
            "reports": sorted(reports, key=lambda x: x['created'], reverse=True),
            "total": len(reports)
        }), 200
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


@app.route("/api/reports/download/<filename>", methods=["GET"])
def download_report(filename):
    """Download a specific report"""
    try:
        report_path = Config.REPORTS_DIR / filename
        
        if not report_path.exists():
            return jsonify({
                "status": "error",
                "error": "Report not found"
            }), 404
        
        return send_file(report_path, as_attachment=True, download_name=filename)
    
    except Exception as e:
        return jsonify({
            "status": "error",
            "error": str(e)
        }), 500


# =====================
# Error Handlers
# =====================

@app.errorhandler(404)
def not_found(error):
    """Handle 404 errors"""
    return jsonify({
        "status": "error",
        "error": "Endpoint not found"
    }), 404


@app.errorhandler(500)
def internal_error(error):
    """Handle 500 errors"""
    return jsonify({
        "status": "error",
        "error": "Internal server error"
    }), 500


if __name__ == "__main__":
    # Run Flask app
    print("=" * 80)
    print("Developer Workflow Automation Hub")
    print("=" * 80)
    print(f"\nStarting server on http://localhost:5000")
    print(f"API Documentation: http://localhost:5000/api/tasks")
    print(f"Frontend: http://localhost:5000/static/index.html")
    print("\n" + "=" * 80)
    
    app.run(debug=True, host="0.0.0.0", port=5000)
