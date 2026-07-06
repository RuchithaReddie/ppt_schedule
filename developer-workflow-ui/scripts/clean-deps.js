const fs = require('fs');
const path = require('path');

const projectRoot = path.resolve(__dirname, '..');
const pathsToRemove = [
  path.join(projectRoot, 'node_modules'),
  path.join(projectRoot, 'package-lock.json')
];

for (const targetPath of pathsToRemove) {
  if (!fs.existsSync(targetPath)) {
    continue;
  }

  fs.rmSync(targetPath, { recursive: true, force: true });
  console.log(`Removed ${path.relative(projectRoot, targetPath)}`);
}

console.log('Dependency cleanup complete. Run: npm.cmd run install:safe');