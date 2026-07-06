// API Base URL
const API_BASE_URL = "http://localhost:5000/api";

// Tab Switching
function switchTab(tabName) {
    // Hide all tabs
    document.querySelectorAll('.tab-content').forEach(tab => {
        tab.classList.remove('active');
    });

    // Remove active class from all buttons
    document.querySelectorAll('.nav-btn').forEach(btn => {
        btn.classList.remove('active');
    });

    // Show selected tab
    document.getElementById(`${tabName}-tab`).classList.add('active');

    // Add active class to clicked button
    event.target.classList.add('active');

    // Load reports if reports tab
    if (tabName === 'reports') {
        loadReports();
    }
}

// ==================
// Repository Analysis
// ==================

async function analyzeRepository() {
    const repoUrl = document.getElementById('repo-url').value.trim();

    if (!repoUrl) {
        showError('repo-result', 'Please enter a repository URL');
        return;
    }

    const loading = document.getElementById('repo-loading');
    const result = document.getElementById('repo-result');

    loading.style.display = 'block';
    result.innerHTML = '';

    try {
        const response = await fetch(`${API_BASE_URL}/tasks/repository-analysis`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ repo_url: repoUrl })
        });

        const data = await response.json();

        loading.style.display = 'none';

        if (response.ok && data.status === 'success') {
            displayRepositoryResult(data);
        } else {
            showError('repo-result', data.error || 'Failed to analyze repository');
        }
    } catch (error) {
        loading.style.display = 'none';
        showError('repo-result', `Error: ${error.message}`);
    }
}

function displayRepositoryResult(data) {
    const result = document.getElementById('repo-result');
    const repo = data.result;

    let html = `
        <div class="result-success">
            <h3>✅ Repository Analysis Complete</h3>
            <div class="metadata">
                <div class="metadata-item">
                    <span class="metadata-label">Repository:</span>
                    <span class="metadata-value">${repo.repository_name}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Owner:</span>
                    <span class="metadata-value">${repo.owner}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Stars:</span>
                    <span class="metadata-value">${repo.stars || 0}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Forks:</span>
                    <span class="metadata-value">${repo.forks || 0}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Open Issues:</span>
                    <span class="metadata-value">${repo.open_issues || 0}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Primary Language:</span>
                    <span class="metadata-value">${repo.language || 'Not specified'}</span>
                </div>
            </div>
        </div>

        <div class="result-info">
            <h3>📝 Description</h3>
            <p>${repo.description || 'No description available'}</p>
        </div>

        <div class="result-info">
            <h3>🛠️ Technologies Used</h3>
            <ul>
                ${Object.keys(repo.languages || {})
                    .map(lang => `<li>${lang}: ${repo.languages[lang]} lines</li>`)
                    .join('')}
            </ul>
        </div>

        ${repo.topics && repo.topics.length > 0 ? `
            <div class="result-info">
                <h3>🏷️ Topics</h3>
                <ul>
                    ${repo.topics.map(topic => `<li>${topic}</li>`).join('')}
                </ul>
            </div>
        ` : ''}

        <button onclick="downloadReport('${data.report_path}')" class="btn btn-success btn-small">📥 Download Report</button>
    `;

    result.innerHTML = html;
}

// ==================
// Documentation
// ==================

async function generateDocumentation() {
    const repoUrl = document.getElementById('doc-repo-url').value.trim();

    if (!repoUrl) {
        showError('doc-result', 'Please enter a repository URL');
        return;
    }

    const loading = document.getElementById('doc-loading');
    const result = document.getElementById('doc-result');

    loading.style.display = 'block';
    result.innerHTML = '';

    try {
        const response = await fetch(`${API_BASE_URL}/tasks/documentation`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ repo_url: repoUrl })
        });

        const data = await response.json();

        loading.style.display = 'none';

        if (response.ok && data.status === 'success') {
            displayDocumentationResult(data);
        } else {
            showError('doc-result', data.error || 'Failed to generate documentation');
        }
    } catch (error) {
        loading.style.display = 'none';
        showError('doc-result', `Error: ${error.message}`);
    }
}

function displayDocumentationResult(data) {
    const result = document.getElementById('doc-result');
    const doc = data.result;

    let html = `
        <div class="result-success">
            <h3>✅ Documentation Generated Successfully</h3>
            <div class="metadata">
                <div class="metadata-item">
                    <span class="metadata-label">Project:</span>
                    <span class="metadata-value">${doc.project_name}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Stars:</span>
                    <span class="metadata-value">${doc.statistics?.stars || 0}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Forks:</span>
                    <span class="metadata-value">${doc.statistics?.forks || 0}</span>
                </div>
            </div>
        </div>

        <div class="result-info">
            <h3>📋 Description</h3>
            <p>${doc.description}</p>
        </div>

        <div class="result-info">
            <h3>⭐ Key Features</h3>
            <ul>
                ${doc.features.map(f => `<li>${f}</li>`).join('')}
            </ul>
        </div>

        <div class="result-info">
            <h3>💻 Technologies</h3>
            <ul>
                ${doc.technologies.map(t => `<li>${t}</li>`).join('')}
            </ul>
        </div>

        <div class="result-info">
            <h3>🚀 Setup Instructions</h3>
            <pre>${doc.usage}</pre>
        </div>

        <button onclick="downloadReport('${data.report_path}')" class="btn btn-success btn-small">📥 Download Report</button>
    `;

    result.innerHTML = html;
}

// ==================
// Code Quality
// ==================

async function analyzeCodeQuality() {
    const fileInput = document.getElementById('code-file');

    if (!fileInput.files || fileInput.files.length === 0) {
        showError('quality-result', 'Please select a file');
        return;
    }

    const file = fileInput.files[0];
    const loading = document.getElementById('quality-loading');
    const resultDiv = document.getElementById('quality-result');

    loading.style.display = 'block';
    resultDiv.innerHTML = '';

    const formData = new FormData();
    formData.append('file', file);

    try {
        const response = await fetch(`${API_BASE_URL}/tasks/code-quality`, {
            method: 'POST',
            body: formData
        });

        const data = await response.json();

        loading.style.display = 'none';

        if (response.ok && data.status === 'success') {
            displayCodeQualityResult(data);
        } else {
            showError('quality-result', data.error || 'Failed to analyze code');
        }
    } catch (error) {
        loading.style.display = 'none';
        showError('quality-result', `Error: ${error.message}`);
    }
}

function displayCodeQualityResult(data) {
    const result = document.getElementById('quality-result');
    const analysis = data.result;

    const scoreColor = analysis.overall_score >= 80 ? 'green' : 
                      analysis.overall_score >= 60 ? 'orange' : 'red';

    let html = `
        <div class="result-success">
            <h3>✅ Code Quality Analysis Complete</h3>
            <div class="metadata">
                <div class="metadata-item">
                    <span class="metadata-label">File:</span>
                    <span class="metadata-value">${analysis.file_name}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Quality Score:</span>
                    <span class="metadata-value" style="color: ${scoreColor}; font-weight: bold;">
                        ${analysis.overall_score}/100
                    </span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Total Issues:</span>
                    <span class="metadata-value">${analysis.total_issues}</span>
                </div>
            </div>
        </div>
    `;

    if (analysis.issues && analysis.issues.length > 0) {
        html += `
            <div class="result-info">
                <h3>🔍 Issues Found</h3>
                <ul>
                    ${analysis.issues.map(issue => `
                        <li>
                            <strong>${issue.type}</strong> (${issue.severity}) - Line ${issue.line}
                            <br><small>${issue.suggestion}</small>
                        </li>
                    `).join('')}
                </ul>
            </div>
        `;
    } else {
        html += `
            <div class="result-success">
                <h3>🎉 No Issues Found!</h3>
                <p>Your code looks great!</p>
            </div>
        `;
    }

    if (analysis.recommendations && analysis.recommendations.length > 0) {
        html += `
            <div class="result-info">
                <h3>💡 Recommendations</h3>
                <ul>
                    ${analysis.recommendations.map(rec => `<li>${rec}</li>`).join('')}
                </ul>
            </div>
        `;
    }

    html += `<button onclick="downloadReport('${data.report_path}')" class="btn btn-success btn-small">📥 Download Report</button>`;

    result.innerHTML = html;
}

// ==================
// Workflow
// ==================

async function executeWorkflow() {
    const repoUrl = document.getElementById('workflow-repo-url').value.trim();

    if (!repoUrl) {
        showError('workflow-result', 'Please enter a repository URL');
        return;
    }

    const loading = document.getElementById('workflow-loading');
    const result = document.getElementById('workflow-result');

    loading.style.display = 'block';
    result.innerHTML = '';

    try {
        const response = await fetch(`${API_BASE_URL}/workflow/execute`, {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({ repo_url: repoUrl })
        });

        const data = await response.json();

        loading.style.display = 'none';

        if (response.ok && data.status === 'completed') {
            displayWorkflowResult(data);
        } else {
            showError('workflow-result', data.error || 'Workflow execution failed');
        }
    } catch (error) {
        loading.style.display = 'none';
        showError('workflow-result', `Error: ${error.message}`);
    }
}

function displayWorkflowResult(data) {
    const result = document.getElementById('workflow-result');

    let html = `
        <div class="result-success">
            <h3>✅ Workflow Execution Complete</h3>
            <div class="metadata">
                <div class="metadata-item">
                    <span class="metadata-label">Status:</span>
                    <span class="metadata-value">${data.status.toUpperCase()}</span>
                </div>
                <div class="metadata-item">
                    <span class="metadata-label">Duration:</span>
                    <span class="metadata-value">${data.duration_seconds?.toFixed(2) || 0}s</span>
                </div>
            </div>
        </div>

        <div class="result-info">
            <h3>📊 Task Results</h3>
    `;

    Object.keys(data.results).forEach((taskName, index) => {
        if (taskName !== 'error') {
            const taskData = data.results[taskName];
            const metadata = taskData.metadata;
            html += `
                <div class="metadata">
                    <div class="metadata-item">
                        <span class="metadata-label">Task ${index + 1}: ${taskName}</span>
                        <span class="metadata-value">${metadata.status}</span>
                    </div>
                    <div class="metadata-item">
                        <span class="metadata-label">Duration:</span>
                        <span class="metadata-value">${metadata.duration_seconds?.toFixed(2) || 0}s</span>
                    </div>
                </div>
            `;
        }
    });

    html += `
            </div>
            <button onclick="downloadReport('${data.report_path}')" class="btn btn-success btn-small">📥 Download Report</button>
    `;

    result.innerHTML = html;
}

// ==================
// Reports
// ==================

async function loadReports() {
    const loading = document.getElementById('reports-loading');
    const container = document.getElementById('reports-list');

    loading.style.display = 'block';
    container.innerHTML = '';

    try {
        const response = await fetch(`${API_BASE_URL}/reports`);
        const data = await response.json();

        loading.style.display = 'none';

        if (response.ok && data.status === 'success') {
            displayReports(data.reports);
        } else {
            container.innerHTML = '<div class="result-error"><p>Failed to load reports</p></div>';
        }
    } catch (error) {
        loading.style.display = 'none';
        container.innerHTML = `<div class="result-error"><p>Error: ${error.message}</p></div>`;
    }
}

function displayReports(reports) {
    const container = document.getElementById('reports-list');

    if (reports.length === 0) {
        container.innerHTML = '<div class="result-info"><p>No reports generated yet. Execute tasks to generate reports.</p></div>';
        return;
    }

    let html = '<div class="reports-container">';

    reports.forEach(report => {
        const date = new Date(report.created * 1000).toLocaleString();
        html += `
            <div class="report-card">
                <h4>📄 ${report.name}</h4>
                <p><small>${date}</small></p>
                <a href="${report.path}" class="btn btn-primary btn-small" download>⬇️ Download</a>
            </div>
        `;
    });

    html += '</div>';
    container.innerHTML = html;
}

// ==================
// Utilities
// ==================

function showError(elementId, message) {
    const element = document.getElementById(elementId);
    element.innerHTML = `
        <div class="result-error">
            <h3>❌ Error</h3>
            <p>${message}</p>
        </div>
    `;
}

function downloadReport(path) {
    // Extract filename from path
    const filename = path.split('/').pop();
    window.location.href = `http://localhost:5000/api/reports/download/${filename}`;
}

// Initialize
document.addEventListener('DOMContentLoaded', () => {
    console.log('Developer Workflow Automation Hub loaded successfully');
});
