const http = require('http');
const fs = require('fs');
const path = require('path');

const port = Number(process.env.FALLBACK_PORT || 4200);
const backendUrl = process.env.BACKEND_URL || 'http://localhost:8080';
const indexPath = path.join(__dirname, 'fallback-index.html');

const server = http.createServer((request, response) => {
  if (request.url.startsWith('/api')) {
    proxyToBackend(request, response);
    return;
  }

  if (request.method === 'GET') {
    serveIndex(response);
    return;
  }

  response.writeHead(404, { 'Content-Type': 'text/plain; charset=utf-8' });
  response.end('Not found');
});

function serveIndex(response) {
  fs.readFile(indexPath, 'utf8', (error, content) => {
    if (error) {
      response.writeHead(500, { 'Content-Type': 'text/plain; charset=utf-8' });
      response.end('Unable to load fallback UI.');
      return;
    }

    response.writeHead(200, {
      'Content-Type': 'text/html; charset=utf-8',
      'Cache-Control': 'no-store, no-cache, must-revalidate, proxy-revalidate',
      'Pragma': 'no-cache',
      'Expires': '0'
    });
    response.end(content);
  });
}

function proxyToBackend(request, response) {
  const target = new URL(request.url, backendUrl);
  const proxyRequest = http.request(target, {
    method: request.method,
    headers: {
      ...request.headers,
      host: target.host
    }
  }, (proxyResponse) => {
    response.writeHead(proxyResponse.statusCode || 500, proxyResponse.headers);
    proxyResponse.pipe(response);
  });

  proxyRequest.on('error', () => {
    response.writeHead(502, { 'Content-Type': 'application/json; charset=utf-8' });
    response.end(JSON.stringify({ error: `Backend is not reachable at ${backendUrl}.` }));
  });

  request.pipe(proxyRequest);
}

server.listen(port, () => {
  console.log(`Fallback demo UI running at http://localhost:${port}`);
  console.log(`Proxying API requests to ${backendUrl}`);
});
