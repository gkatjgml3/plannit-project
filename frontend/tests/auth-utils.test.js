const test = require("node:test");
const assert = require("node:assert/strict");
const {
  hasRequiredConsents,
  validateLoginInput,
  validateSignupInput,
} = require("../js/auth-utils.js");

test("로그인 아이디와 비밀번호가 비어 있으면 두 필드를 안내한다", () => {
  const errors = validateLoginInput({ loginId: "", password: "" });
  assert.deepEqual(Object.keys(errors).sort(), ["loginId", "loginPassword"]);
});

test("회원가입 이메일과 비밀번호 규칙을 검증한다", () => {
  const errors = validateSignupInput({
    name: "김래원",
    email: "invalid-email",
    password: "password",
    passwordConfirm: "different",
  });
  assert.deepEqual(Object.keys(errors).sort(), ["signupEmail", "signupPassword", "signupPasswordConfirm"]);
});

test("올바른 회원가입 정보는 오류가 없다", () => {
  const errors = validateSignupInput({
    name: "김래원",
    email: "student@example.com",
    password: "planit2026",
    passwordConfirm: "planit2026",
  });
  assert.deepEqual(errors, {});
});

test("필수 약관 두 항목이 모두 선택되어야 한다", () => {
  assert.equal(hasRequiredConsents({ serviceTerms: true, privacyTerms: false }), false);
  assert.equal(hasRequiredConsents({ serviceTerms: true, privacyTerms: true }), true);
});
