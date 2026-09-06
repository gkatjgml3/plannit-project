const test = require("node:test");
const assert = require("node:assert/strict");
const {
  createGreeting,
  hasRequiredConsents,
  isDemoAccount,
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
    signupId: "planit01",
    email: "invalid-email",
    password: "password",
    passwordConfirm: "different",
  });
  assert.deepEqual(Object.keys(errors).sort(), ["signupEmail", "signupPassword", "signupPasswordConfirm"]);
});

test("올바른 회원가입 정보는 오류가 없다", () => {
  const errors = validateSignupInput({
    name: "김래원",
    signupId: "planit01",
    email: "student@example.com",
    password: "planit2026",
    passwordConfirm: "planit2026",
  });
  assert.deepEqual(errors, {});
});

test("회원가입 아이디는 영문과 숫자로 4자 이상이어야 한다", () => {
  const errors = validateSignupInput({
    name: "김래원",
    signupId: "가나",
    email: "student@example.com",
    password: "planit2026",
    passwordConfirm: "planit2026",
  });
  assert.match(errors.signupId, /영문과 숫자/);
});

test("필수 약관 두 항목이 모두 선택되어야 한다", () => {
  assert.equal(hasRequiredConsents({ serviceTerms: true, privacyTerms: false }), false);
  assert.equal(hasRequiredConsents({ serviceTerms: true, privacyTerms: true }), true);
});

test("지정 이메일로 로그인할 때만 데모 계정으로 구분한다", () => {
  assert.equal(isDemoAccount("s2653@e-mirim.hs.kr"), true);
  assert.equal(isDemoAccount(" S2653@E-MIRIM.HS.KR "), true);
  assert.equal(isDemoAccount("student@example.com"), false);
});

test("회원가입 이름으로 대시보드 인사말을 만든다", () => {
  assert.equal(createGreeting("지민"), "지민님, 안녕하세요!");
  assert.equal(createGreeting(""), "ㅇㅇ님, 안녕하세요!");
});
