(function registerAuthUtils(globalObject) {
  "use strict";

  const demoAccountEmail = "s2653@e-mirim.hs.kr";

  function isDemoAccount(loginId) {
    return (loginId?.trim().toLowerCase() || "") === demoAccountEmail;
  }

  function createGreeting(name) {
    const displayName = name?.trim() || "ㅇㅇ";
    return `${displayName}님, 안녕하세요!`;
  }

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

  function hasRequiredConsents(consents) {
    return Boolean(consents.serviceTerms && consents.privacyTerms);
  }

  const authUtils = { createGreeting, hasRequiredConsents, isDemoAccount, validateLoginInput, validateSignupInput };
  globalObject.PlanitAuthUtils = authUtils;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = authUtils;
  }
})(typeof window !== "undefined" ? window : globalThis);
