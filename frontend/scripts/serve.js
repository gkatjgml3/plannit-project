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

// 입력값: 파일의 확장자를 포함한 경로
// 출력값: 브라우저 응답에 사용할 콘텐츠 유형
// 기능: 정적 파일 종류에 맞는 HTTP Content-Type을 선택한다.
function getContentType(filePath) {
  return contentTypes[path.extname(filePath).toLowerCase()] || "application/octet-stream";
}

// 입력값: HTTP 요청과 응답 객체
// 출력값: 없음
// 기능: 빌드된 로그인 화면 파일을 로컬 미리보기로 제공한다.
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
  console.log(`PLANNIT preview: http://127.0.0.1:${port}`);
});
