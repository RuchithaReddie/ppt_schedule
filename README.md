# Developer Workflow Automation Hub

This workspace contains a Spring Boot backend and an Angular frontend for running offline developer workflow tasks.

For the interview/demo architecture explanation, see [ARCHITECTURE.md](ARCHITECTURE.md).

## Run the Backend

From the workspace root:

```powershell
cd developer-workflow-api
mvn.cmd spring-boot:run
```

The backend runs on `http://localhost:8080` and exposes:

- `GET /api/tasks`
- `POST /api/tasks/execute`

Generated reports are saved in `developer-workflow-api/reports`.

## Install Frontend Dependencies on Restricted Networks

The Angular project includes a local `.npmrc` configured for company or college networks:

```text
registry=https://registry.npmjs.org/
legacy-peer-deps=true
audit=false
fund=false
progress=false
```

Use `npm.cmd` on Windows PowerShell to avoid `npm.ps1` execution-policy problems:

```powershell
cd developer-workflow-ui
npm.cmd run install:safe
```

If `node_modules` is missing or the install gets into a bad state, safely regenerate dependencies:

```powershell
cd developer-workflow-ui
npm.cmd run clean:deps
npm.cmd cache clean --force
npm.cmd run install:safe
```

Do not copy `node_modules` from another machine. Regenerate dependencies from `package.json` and `package-lock.json` when available.

## Run the Angular Frontend

After dependencies install successfully:

```powershell
cd developer-workflow-ui
npm.cmd start
```

The UI runs on `http://localhost:4200`.

The proxy configuration forwards frontend API calls from `/api` to the backend:

```text
/api -> http://localhost:8080
```

## Start Backend and Frontend Together

Open two terminals from the workspace root.

Terminal 1:

```powershell
cd developer-workflow-api
mvn.cmd spring-boot:run
```

Terminal 2:

```powershell
cd developer-workflow-ui
npm.cmd run install:safe
npm.cmd start
```

Then open `http://localhost:4200`.

## Troubleshoot npm Hangs or EACCES Errors

First confirm Node and npm work:

```powershell
cd developer-workflow-ui
node -v
npm.cmd -v
npm.cmd config get registry
```

If `npm install` hangs or fails with `EACCES`, try:

```powershell
npm.cmd cache clean --force
npm.cmd config set registry https://registry.npmjs.org/
npm.cmd run install:safe
```

If your company network requires a proxy, configure it explicitly:

```powershell
npm.cmd config set proxy http://your-proxy-host:your-proxy-port
npm.cmd config set https-proxy http://your-proxy-host:your-proxy-port
npm.cmd run install:safe
```

If antivirus or endpoint protection blocks `node.exe`, ask IT to allow Node.js/npm traffic to `https://registry.npmjs.org/`.

