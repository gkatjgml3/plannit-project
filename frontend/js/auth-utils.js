(function registerAuthUtils(globalObject) {
  "use strict";

  // 입력값: 아이디와 비밀번호를 담은 객체
  // 출력값: 필드별 사용자용 오류 메시지 객체
  // 기능: 로그인 요청 전에 필수 입력과 기본 길이를 검증한다.
  function validateLoginInput(credentials) {
    const errors = {};
    const loginId = credentials.loginId?.trim() || "";
    const password = credentials.password || "";

    if (!loginId) {
      errors.loginId = "아이디 또는 이메일을 입력해 주세요.";
    } else if (loginId.length < 3) {
      errors.loginId = "아이디는 3자 이상 입력해 주세요.";
    }

    if (!password) {
      errors.password = "비밀번호를 입력해 주세요.";
    } else if (password.length < 6) {
      errors.password = "비밀번호는 6자 이상 입력해 주세요.";
    }

    return errors;
  }

  const authUtils = { validateLoginInput };
  globalObject.PlannitAuthUtils = authUtils;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = authUtils;
  }
})(typeof window !== "undefined" ? window : globalThis);
