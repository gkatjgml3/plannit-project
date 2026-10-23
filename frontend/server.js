const http = require("http");
const fs = require("fs");
const path = require("path");

const port = Number(process.env.PLANIT_PORT) || 4173;
const projectRoot = path.resolve(__dirname, "..");
const contentTypes = {
  ".html": "text/html; charset=utf-8",
  ".css": "text/css; charset=utf-8",
  ".js": "text/javascript; charset=utf-8",
  ".png": "image/png",
  ".jpg": "image/jpeg",
  ".jpeg": "image/jpeg",
  ".svg": "image/svg+xml",
};

// 입력값: HTTP 요청과 응답 객체
// 출력값: 없음
// 기능: PLANIT 프론트엔드 파일만 프로젝트 루트에서 안전하게 제공한다.
function handleRequest(request, response) {
  const requestPath = decodeURIComponent(request.url.split("?")[0]);
  const relativePath = requestPath === "/" ? "frontend/index.html" : requestPath.replace(/^\/+/, "");
  const filePath = path.resolve(projectRoot, relativePath);

  if (!filePath.startsWith(`${projectRoot}${path.sep}`)) {
    response.writeHead(403);
    response.end("Forbidden");
    return;
  }

  fs.readFile(filePath, (error, data) => {
    if (error) {
      response.writeHead(404);
      response.end("Not found");
      return;
    }
    response.setHeader("Content-Type", contentTypes[path.extname(filePath).toLowerCase()] ?? "application/octet-stream");
    response.end(data);
  });
}

const server = http.createServer(handleRequest);
server.listen(port, "127.0.0.1", () => {
  console.log(`PLANIT: http://localhost:${port}/frontend/index.html`);
});
