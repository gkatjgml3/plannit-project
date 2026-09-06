const fs = require("node:fs");
const path = require("node:path");

const frontendRoot = path.resolve(__dirname, "..");
const outputRoot = path.join(frontendRoot, "dist");
const sourceEntries = ["index.html", "css", "js", "assets"];

// 입력값: 원본 경로와 대상 경로
// 출력값: 없음
// 기능: 정적 프론트엔드 파일과 폴더를 배포 폴더로 복사한다.
function copyEntry(sourcePath, destinationPath) {
  fs.cpSync(sourcePath, destinationPath, { recursive: true });
}

// 입력값: 없음
// 출력값: 없음
// 기능: 이전 결과를 지우고 실행 가능한 정적 배포 폴더를 만든다.
function buildFrontend() {
  fs.rmSync(outputRoot, { recursive: true, force: true });
  fs.mkdirSync(outputRoot, { recursive: true });

  sourceEntries.forEach((entry) => {
    copyEntry(path.join(frontendRoot, entry), path.join(outputRoot, entry));
  });

  console.log(`PLANNIT frontend build complete: ${outputRoot}`);
}

buildFrontend();
