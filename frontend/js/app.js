(function startLoginPage() {
  "use strict";

  const authUtils = window.PlannitAuthUtils;
  const elements = {
    currentYear: document.querySelector("#currentYear"),
    loginForm: document.querySelector("#loginForm"),
    loginId: document.querySelector("#loginId"),
    loginIdError: document.querySelector("#loginIdError"),
    password: document.querySelector("#password"),
    passwordError: document.querySelector("#passwordError"),
    passwordToggle: document.querySelector("#passwordToggle"),
    loginButton: document.querySelector("#loginButton"),
    formNotice: document.querySelector("#formNotice"),
    forgotPasswordButton: document.querySelector("#forgotPasswordButton"),
    signupButton: document.querySelector("#signupButton"),
    toast: document.querySelector("#toast"),
  };
  let toastTimer = null;
  let loginTimer = null;

  // 입력값: 사용자에게 보여 줄 짧은 안내 문장
  // 출력값: 없음
  // 기능: 아직 연결되지 않은 보조 기능의 상태를 잠시 알린다.
  function showToast(message) {
    window.clearTimeout(toastTimer);
    elements.toast.textContent = message;
    elements.toast.classList.add("is-visible");
    toastTimer = window.setTimeout(() => elements.toast.classList.remove("is-visible"), 2400);
  }

  // 입력값: 필드 이름과 오류 메시지
  // 출력값: 없음
  // 기능: 해당 입력창에 접근 가능한 오류 상태를 표시한다.
  function showFieldError(fieldName, message) {
    const input = elements[fieldName];
    const errorElement = elements[`${fieldName}Error`];
    input.setAttribute("aria-invalid", String(Boolean(message)));
    errorElement.textContent = message || "";
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 비밀번호 입력값을 보이거나 가리고 버튼 상태를 함께 갱신한다.
  function togglePasswordVisibility() {
    const shouldShow = elements.password.type === "password";
    elements.password.type = shouldShow ? "text" : "password";
    elements.passwordToggle.textContent = shouldShow ? "숨김" : "보기";
    elements.passwordToggle.setAttribute("aria-pressed", String(shouldShow));
    elements.passwordToggle.setAttribute("aria-label", shouldShow ? "비밀번호 숨기기" : "비밀번호 표시");
    elements.password.focus();
  }

  // 입력값: 로그인 폼 제출 이벤트
  // 출력값: 없음
  // 기능: 입력을 검증하고 백엔드 연결 전 상태를 사용자에게 정확히 안내한다.
  function handleLoginSubmit(event) {
    event.preventDefault();
    window.clearTimeout(loginTimer);
    elements.formNotice.classList.remove("is-visible");

    const errors = authUtils.validateLoginInput({
      loginId: elements.loginId.value,
      password: elements.password.value,
    });

    showFieldError("loginId", errors.loginId);
    showFieldError("password", errors.password);

    if (Object.keys(errors).length > 0) {
      const firstInvalidField = errors.loginId ? elements.loginId : elements.password;
      firstInvalidField.focus();
      return;
    }

    elements.loginButton.disabled = true;
    elements.loginButton.classList.add("is-loading");
    loginTimer = window.setTimeout(() => {
      elements.loginButton.disabled = false;
      elements.loginButton.classList.remove("is-loading");
      elements.formNotice.textContent = "로그인 화면이 준비되었습니다. 실제 로그인은 서버 연결 후 사용할 수 있어요.";
      elements.formNotice.classList.add("is-visible");
    }, 650);
  }

  // 입력값: 입력 이벤트
  // 출력값: 없음
  // 기능: 사용자가 다시 입력하면 해당 필드의 이전 오류를 지운다.
  function clearFieldError(event) {
    showFieldError(event.target.id, "");
    elements.formNotice.classList.remove("is-visible");
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 로그인 화면의 버튼과 입력 요소에 동작을 연결한다.
  function bindEvents() {
    elements.loginForm.addEventListener("submit", handleLoginSubmit);
    elements.loginId.addEventListener("input", clearFieldError);
    elements.password.addEventListener("input", clearFieldError);
    elements.passwordToggle.addEventListener("click", togglePasswordVisibility);
    elements.forgotPasswordButton.addEventListener("click", () => {
      showToast("비밀번호 찾기는 인증 기능과 함께 연결할 예정이에요.");
    });
    elements.signupButton.addEventListener("click", () => {
      showToast("회원가입 화면은 다음 작업 범위에서 제작할 수 있어요.");
    });
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 저작권 연도를 갱신하고 로그인 화면을 초기화한다.
  function initialize() {
    elements.currentYear.textContent = String(new Date().getFullYear());
    elements.loginId.setAttribute("aria-invalid", "false");
    elements.password.setAttribute("aria-invalid", "false");
    bindEvents();
  }

  initialize();
})();
