(function registerAuthUtils(globalObject) {
  "use strict";

  const demoAccountEmail = "s2653@e-mirim.hs.kr";

  // 입력값: 로그인 폼에 입력한 아이디 또는 이메일
  // 출력값: 데모 계정 이메일과 정확히 일치하면 true
  // 기능: 정적 데모 화면에 예시 일정을 표시할 계정을 구분한다.
  function isDemoAccount(loginId) {
    return (loginId?.trim().toLowerCase() || "") === demoAccountEmail;
  }

  // 입력값: 회원가입에서 입력한 이름
  // 출력값: 대시보드에 표시할 인사말
  // 기능: 이름이 있으면 이름을 사용하고 없으면 기본 표시값으로 인사말을 만든다.
  function createGreeting(name) {
    const displayName = name?.trim() || "ㅇㅇ";
    return `${displayName}님, 안녕하세요!`;
  }

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
      errors.loginPassword = "비밀번호를 입력해 주세요.";
    } else if (password.length < 6) {
      errors.loginPassword = "비밀번호는 6자 이상 입력해 주세요.";
    }

    return errors;
  }

  // 입력값: 이름, 아이디, 이메일, 비밀번호, 비밀번호 확인을 담은 객체
  // 출력값: 필드별 사용자용 오류 메시지 객체
  // 기능: 회원가입 정보의 필수값, 이메일 형식, 비밀번호 규칙과 일치 여부를 검증한다.
  function validateSignupInput(profile) {
    const errors = {};
    const name = profile.name?.trim() || "";
    const signupId = profile.signupId?.trim() || "";
    const email = profile.email?.trim() || "";
    const password = profile.password || "";
    const passwordConfirm = profile.passwordConfirm || "";

    if (name.length < 2) {
      errors.signupName = "이름을 2자 이상 입력해 주세요.";
    }

    if (!/^[A-Za-z0-9]{4,20}$/.test(signupId)) {
      errors.signupId = "아이디는 영문과 숫자로 4~20자 입력해 주세요.";
    }

    if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
      errors.signupEmail = "올바른 이메일 형식으로 입력해 주세요.";
    }

    if (password.length < 8 || !/[A-Za-z]/.test(password) || !/\d/.test(password)) {
      errors.signupPassword = "영문과 숫자를 포함해 8자 이상 입력해 주세요.";
    }

    if (!passwordConfirm || passwordConfirm !== password) {
      errors.signupPasswordConfirm = "비밀번호가 일치하지 않아요.";
    }

    return errors;
  }

  // 입력값: 서비스 이용약관과 개인정보 약관의 동의 여부
  // 출력값: 필수 약관을 모두 동의했으면 true
  // 기능: 선택 약관과 구분해 회원가입에 필요한 필수 동의를 확인한다.
  function hasRequiredConsents(consents) {
    return Boolean(consents.serviceTerms && consents.privacyTerms);
  }

  const authUtils = { createGreeting, hasRequiredConsents, isDemoAccount, validateLoginInput, validateSignupInput };
  globalObject.PlanitAuthUtils = authUtils;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = authUtils;
  }
})(typeof window !== "undefined" ? window : globalThis);
