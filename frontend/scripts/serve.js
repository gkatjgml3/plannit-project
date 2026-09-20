const http = require("node:http");
const fs = require("node:fs");
const path = require("node:path");

const port = 4173;
const outputRoot = path.resolve(__dirname, "..", "dist");
const contentTypes = {
  ".css": "text/css; charset=utf-8",
  ".html": "text/html; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".png": "image/png",
  ".svg": "image/svg+xml",
};

function getContentType(filePath) {
  return contentTypes[path.extname(filePath).toLowerCase()] || "application/octet-stream";
}

function serveRequest(request, response) {
  const requestPath = decodeURIComponent((request.url || "/").split("?")[0]);
  const relativePath = requestPath === "/" ? "index.html" : requestPath.replace(/^\/+/, "");
  const filePath = path.resolve(outputRoot, relativePath);

  if (!filePath.startsWith(`${outputRoot}${path.sep}`) && filePath !== path.join(outputRoot, "index.html")) {
    response.writeHead(403);
    response.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      response.writeHead(error.code === "ENOENT" ? 404 : 500);
      response.end(error.code === "ENOENT" ? "Not found" : "Server error");
      return;
    }

    response.writeHead(200, { "Content-Type": getContentType(filePath) });
    response.end(data);
  });
}

http.createServer(serveRequest).listen(port, "127.0.0.1", () => {
  console.log(`PLANIT preview: http://127.0.0.1:${port}`);
});
