const fs = require("node:fs");
const path = require("node:path");

const frontendRoot = path.resolve(__dirname, "..");
const outputRoot = path.join(frontendRoot, "dist");
const sourceEntries = ["index.html", "css", "js", "assets"];

function copyEntry(sourcePath, destinationPath) {
  fs.cpSync(sourcePath, destinationPath, { recursive: true });
}

function buildFrontend() {
  fs.rmSync(outputRoot, { recursive: true, force: true });
  fs.mkdirSync(outputRoot, { recursive: true });

  sourceEntries.forEach((entry) => {
    copyEntry(path.join(frontendRoot, entry), path.join(outputRoot, entry));
  });

  console.log(`PLANIT frontend build complete: ${outputRoot}`);
}

buildFrontend();
