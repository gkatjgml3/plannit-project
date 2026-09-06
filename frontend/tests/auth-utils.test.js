const test = require("node:test");
const assert = require("node:assert/strict");
const { validateLoginInput } = require("../js/auth-utils.js");

test("아이디와 비밀번호가 비어 있으면 두 필드를 안내한다", () => {
  const errors = validateLoginInput({ loginId: "", password: "" });
  assert.deepEqual(Object.keys(errors).sort(), ["loginId", "password"]);
});

test("아이디는 공백을 제외하고 3자 이상이어야 한다", () => {
  const errors = validateLoginInput({ loginId: " a ", password: "123456" });
  assert.match(errors.loginId, /3자/);
});

test("비밀번호는 6자 이상이어야 한다", () => {
  const errors = validateLoginInput({ loginId: "student", password: "12345" });
  assert.match(errors.password, /6자/);
});

test("유효한 입력은 오류를 반환하지 않는다", () => {
  const errors = validateLoginInput({ loginId: "student01", password: "secret123" });
  assert.deepEqual(errors, {});
});
