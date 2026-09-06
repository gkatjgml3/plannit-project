(function startAuthFlow() {
  "use strict";

  const authUtils = window.PlanitAuthUtils;
  const calendarUtils = window.PlanitCalendarUtils;
  const profileStorageKey = "planitProfile";
  const today = new Date();
  const viewHistory = [];
  const viewMeta = {
    login: { label: "LOGIN", title: "로그인 | PLANIT" },
    signup: { label: "SIGN UP", title: "회원가입 | PLANIT" },
    consent: { label: "AGREEMENT", title: "약관 동의 | PLANIT" },
  };
  const elements = {
    siteHeader: document.querySelector("#siteHeader"),
    siteFooter: document.querySelector("#siteFooter"),
    authContent: document.querySelector("#authContent"),
    dashboardView: document.querySelector("#dashboardView"),
    logoutButton: document.querySelector("#logoutButton"),
    authViews: document.querySelectorAll("[data-auth-view]"),
    backButton: document.querySelector("#backButton"),
    screenLabel: document.querySelector("#screenLabel"),
    topPrompt: document.querySelector("#topPrompt"),
    currentYear: document.querySelector("#currentYear"),
    loginForm: document.querySelector("#loginForm"),
    loginId: document.querySelector("#loginId"),
    loginIdError: document.querySelector("#loginIdError"),
    loginPassword: document.querySelector("#loginPassword"),
    loginPasswordError: document.querySelector("#loginPasswordError"),
    loginNotice: document.querySelector("#loginNotice"),
    forgotPasswordButton: document.querySelector("#forgotPasswordButton"),
    signupForm: document.querySelector("#signupForm"),
    signupName: document.querySelector("#signupName"),
    signupNameError: document.querySelector("#signupNameError"),
    signupId: document.querySelector("#signupId"),
    signupIdError: document.querySelector("#signupIdError"),
    signupEmail: document.querySelector("#signupEmail"),
    signupEmailError: document.querySelector("#signupEmailError"),
    signupPassword: document.querySelector("#signupPassword"),
    signupPasswordError: document.querySelector("#signupPasswordError"),
    signupPasswordConfirm: document.querySelector("#signupPasswordConfirm"),
    signupPasswordConfirmError: document.querySelector("#signupPasswordConfirmError"),
    consentForm: document.querySelector("#consentForm"),
    consentAll: document.querySelector("#consentAll"),
    consentCheckboxes: document.querySelectorAll(".consent-checkbox"),
    consentError: document.querySelector("#consentError"),
    consentNotice: document.querySelector("#consentNotice"),
    completeSignupButton: document.querySelector("#completeSignupButton"),
    dashboardGreeting: document.querySelector("#dashboardGreeting"),
    largeCalendarHeading: document.querySelector("#largeCalendarHeading"),
    calendarDays: document.querySelector("#calendarDays"),
    previousMonthButton: document.querySelector("#previousMonthButton"),
    nextMonthButton: document.querySelector("#nextMonthButton"),
    todayButton: document.querySelector("#todayButton"),
    upcomingCount: document.querySelector("#upcomingCount"),
    upcomingContent: document.querySelector("#upcomingContent"),
    toast: document.querySelector("#toast"),
  };
  let activeView = "login";
  let currentCalendarDate = new Date(today.getFullYear(), today.getMonth(), 1);
  let shouldShowDemoSchedules = false;
  let pendingSignupProfile = null;
  let toastTimer = null;

  // 입력값: 없음
  // 출력값: 현재 브라우저에 저장된 회원 프로필 또는 null
  // 기능: 비밀번호를 제외한 이름, 아이디, 이메일을 로컬 저장소에서 안전하게 읽는다.
  function readStoredProfile() {
    try {
      const profile = JSON.parse(window.localStorage.getItem(profileStorageKey));
      if (!profile || typeof profile.name !== "string" || typeof profile.signupId !== "string" || typeof profile.email !== "string") {
        return null;
      }
      return profile;
    } catch (error) {
      return null;
    }
  }

  // 입력값: 이름, 아이디, 이메일이 담긴 회원 프로필
  // 출력값: 없음
  // 기능: 회원가입 완료 프로필을 비밀번호 없이 현재 브라우저에 저장한다.
  function saveProfile(profile) {
    try {
      window.localStorage.setItem(profileStorageKey, JSON.stringify(profile));
    } catch (error) {
      showToast("이름을 브라우저에 저장하지 못했습니다.");
    }
  }

  // 입력값: 로그인 폼에 입력한 아이디 또는 이메일
  // 출력값: 입력값과 일치하는 저장 프로필 또는 null
  // 기능: 같은 브라우저에서 가입한 사용자의 표시 이름을 로그인 정보와 연결한다.
  function findStoredProfile(loginId) {
    const profile = readStoredProfile();
    const normalizedLoginId = loginId.trim().toLowerCase();
    if (!profile) {
      return null;
    }
    return [profile.signupId, profile.email].some((value) => value.toLowerCase() === normalizedLoginId) ? profile : null;
  }

  // 입력값: 이동할 화면 이름과 이전 화면 기록 여부
  // 출력값: 없음
  // 기능: 로그인, 회원가입, 동의 화면을 전환하고 화면 제목과 탐색 요소를 갱신한다.
  function showView(viewName, shouldRemember = true) {
    if (!viewMeta[viewName] || viewName === activeView) {
      return;
    }

    if (shouldRemember) {
      viewHistory.push(activeView);
    }
    activeView = viewName;

    elements.authViews.forEach((view) => {
      view.classList.toggle("is-hidden", view.dataset.authView !== viewName);
    });
    elements.backButton.classList.toggle("is-hidden", viewName === "login");
    elements.screenLabel.textContent = viewMeta[viewName].label;
    document.title = viewMeta[viewName].title;
    window.history.replaceState(null, "", `#${viewName}`);

    if (viewName === "login") {
      elements.topPrompt.innerHTML = '계정이 없으신가요? <a class="inline-button" href="#signup" data-view-target="signup">회원가입</a>';
    } else {
      elements.topPrompt.innerHTML = '이미 계정이 있으신가요? <a class="inline-button" href="#login" data-view-target="login">로그인</a>';
    }

    elements.authContent.focus({ preventScroll: true });
  }

  // 입력값: 없음
  // 출력값: 현재 연월에 연결된 데모 일정 목록
  // 기능: 실제 현재 월을 기준으로 개인정보가 없는 세 개의 데모 일정을 만든다.
  function getDemoSchedules() {
    return [
      { day: 9, title: "수행평가 제출" },
      { day: 14, title: "동아리 회의" },
      { day: 22, title: "영어 시험" },
    ].map((schedule) => ({
      ...schedule,
      year: today.getFullYear(),
      monthIndex: today.getMonth(),
      isoDate: `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(schedule.day).padStart(2, "0")}`,
    }));
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 선택된 연월의 실제 날짜 42개와 오늘 표시, 데모 일정을 캘린더에 그린다.
  function renderCalendar() {
    const year = currentCalendarDate.getFullYear();
    const monthIndex = currentCalendarDate.getMonth();
    const monthLabel = calendarUtils.createMonthLabel(year, monthIndex);
    const demoSchedules = shouldShowDemoSchedules ? getDemoSchedules() : [];

    elements.largeCalendarHeading.textContent = monthLabel;
    elements.calendarDays.setAttribute("aria-label", `${monthLabel} 월간 캘린더`);
    elements.calendarDays.replaceChildren();

    calendarUtils.buildCalendarDays(year, monthIndex, today).forEach((dayInfo) => {
      const calendarCell = document.createElement("div");
      calendarCell.className = "calendar-cell";
      calendarCell.setAttribute("role", "gridcell");
      calendarCell.dataset.date = dayInfo.isoDate;
      calendarCell.classList.toggle("is-other", dayInfo.isOtherMonth);
      calendarCell.classList.toggle("is-today", dayInfo.isToday);

      const dayNumber = document.createElement("b");
      dayNumber.textContent = String(dayInfo.day);
      calendarCell.append(dayNumber);

      const schedule = demoSchedules.find((item) => item.isoDate === dayInfo.isoDate);
      if (schedule) {
        const calendarEvent = document.createElement("span");
        calendarEvent.className = "calendar-event";
        calendarEvent.textContent = schedule.title;
        calendarCell.append(calendarEvent);
      }
      elements.calendarDays.append(calendarCell);
    });
  }

  // 입력값: 데모 일정 표시 여부
  // 출력값: 없음
  // 기능: 지정된 데모 계정에는 예시 일정을, 나머지 화면에는 빈 상태를 표시한다.
  function renderDashboardContent(shouldShowDemo) {
    const demoSchedules = getDemoSchedules();
    shouldShowDemoSchedules = shouldShowDemo;
    elements.upcomingContent.replaceChildren();
    elements.upcomingContent.classList.toggle("upcoming-empty", !shouldShowDemo);
    elements.upcomingContent.classList.toggle("upcoming-list", shouldShowDemo);
    elements.upcomingContent.setAttribute("aria-label", shouldShowDemo ? "데모 일정" : "등록된 일정 없음");
    elements.upcomingCount.textContent = shouldShowDemo ? String(demoSchedules.length) : "0";
    renderCalendar();

    if (!shouldShowDemo) {
      return;
    }

    demoSchedules.forEach((schedule) => {
      const upcomingItem = document.createElement("article");
      upcomingItem.className = "upcoming-item";
      const scheduleDate = document.createElement("time");
      scheduleDate.dateTime = schedule.isoDate;
      const scheduleDay = document.createElement("b");
      scheduleDay.textContent = `${schedule.day}일`;
      const scheduleMonth = document.createElement("span");
      scheduleMonth.textContent = `${schedule.monthIndex + 1}월`;
      scheduleDate.append(scheduleDay, scheduleMonth);
      const scheduleTitle = document.createElement("h3");
      scheduleTitle.textContent = schedule.title;
      upcomingItem.append(scheduleDate, scheduleTitle);
      elements.upcomingContent.append(upcomingItem);
    });
  }

  // 입력값: 이동할 월 수
  // 출력값: 없음
  // 기능: 이전 또는 다음 달로 이동하고 1월과 12월 경계에서 연도도 함께 변경한다.
  function moveCalendarMonth(amount) {
    currentCalendarDate = calendarUtils.moveMonth(currentCalendarDate, amount);
    renderCalendar();
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 캘린더를 실제 오늘이 포함된 연월로 되돌린다.
  function showCurrentMonth() {
    currentCalendarDate = new Date(today.getFullYear(), today.getMonth(), 1);
    renderCalendar();
  }

  // 입력값: 데모 일정 표시 여부와 화면에 표시할 이름
  // 출력값: 없음
  // 기능: 정적 인증 화면을 숨기고 메인 대시보드를 표시한다.
  function showDashboard(shouldShowDemo = false, displayName = "") {
    renderDashboardContent(shouldShowDemo);
    elements.dashboardGreeting.textContent = authUtils.createGreeting(displayName);
    elements.siteHeader.classList.add("is-hidden");
    elements.authContent.classList.add("is-hidden");
    elements.siteFooter.classList.add("is-hidden");
    elements.dashboardView.classList.remove("is-hidden");
    document.title = "메인 대시보드 | PLANIT";
    window.history.replaceState(null, "", "#dashboard");
    elements.dashboardView.focus({ preventScroll: true });
    window.scrollTo({ top: 0, behavior: "smooth" });
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 대시보드를 닫고 로그인 화면으로 돌아간다.
  function showLoginFromDashboard() {
    elements.dashboardView.classList.add("is-hidden");
    elements.siteHeader.classList.remove("is-hidden");
    elements.authContent.classList.remove("is-hidden");
    elements.siteFooter.classList.remove("is-hidden");
    viewHistory.length = 0;
    if (activeView === "login") {
      document.title = viewMeta.login.title;
      elements.authContent.focus({ preventScroll: true });
    } else {
      showView("login", false);
    }
  }

  // 입력값: 사용자에게 보여 줄 짧은 안내 문장
  // 출력값: 없음
  // 기능: 준비 중인 기능이나 약관 상세 상태를 잠시 알린다.
  function showToast(message) {
    window.clearTimeout(toastTimer);
    elements.toast.textContent = message;
    elements.toast.classList.add("is-visible");
    toastTimer = window.setTimeout(() => elements.toast.classList.remove("is-visible"), 2400);
  }

  // 입력값: 입력 요소의 id와 오류 메시지
  // 출력값: 없음
  // 기능: 입력창과 연결된 오류 영역에 접근 가능한 검증 결과를 표시한다.
  function showFieldError(fieldId, message) {
    const input = elements[fieldId];
    const errorElement = elements[`${fieldId}Error`];
    input.setAttribute("aria-invalid", String(Boolean(message)));
    errorElement.textContent = message || "";
  }

  // 입력값: 로그인 폼 제출 이벤트
  // 출력값: 없음
  // 기능: 로그인 입력을 검증하고 정적 화면에서 동작 확인 결과를 알린다.
  function handleLoginSubmit(event) {
    event.preventDefault();
    elements.loginNotice.classList.remove("is-visible");
    const errors = authUtils.validateLoginInput({
      loginId: elements.loginId.value,
      password: elements.loginPassword.value,
    });

    showFieldError("loginId", errors.loginId);
    showFieldError("loginPassword", errors.loginPassword);

    if (Object.keys(errors).length > 0) {
      (errors.loginId ? elements.loginId : elements.loginPassword).focus();
      return;
    }

    const storedProfile = findStoredProfile(elements.loginId.value);
    showDashboard(authUtils.isDemoAccount(elements.loginId.value), storedProfile?.name);
  }

  // 입력값: 회원가입 폼 제출 이벤트
  // 출력값: 없음
  // 기능: 가입 정보를 검증한 뒤 약관 동의 화면으로 이동한다.
  function handleSignupSubmit(event) {
    event.preventDefault();
    const errors = authUtils.validateSignupInput({
      name: elements.signupName.value,
      signupId: elements.signupId.value,
      email: elements.signupEmail.value,
      password: elements.signupPassword.value,
      passwordConfirm: elements.signupPasswordConfirm.value,
    });
    const fieldIds = ["signupName", "signupId", "signupEmail", "signupPassword", "signupPasswordConfirm"];

    fieldIds.forEach((fieldId) => showFieldError(fieldId, errors[fieldId]));
    if (Object.keys(errors).length > 0) {
      elements[fieldIds.find((fieldId) => errors[fieldId])].focus();
      return;
    }

    pendingSignupProfile = {
      name: elements.signupName.value.trim(),
      signupId: elements.signupId.value.trim(),
      email: elements.signupEmail.value.trim(),
    };
    showView("consent");
  }

  // 입력값: 없음
  // 출력값: 필수 약관의 현재 동의 여부
  // 기능: 동의 체크박스 상태에서 필수 항목 값을 모은다.
  function getConsentState() {
    return {
      serviceTerms: elements.consentForm.elements.serviceTerms.checked,
      privacyTerms: elements.consentForm.elements.privacyTerms.checked,
    };
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 개별 약관 상태에 맞춰 전체 동의와 가입 버튼 상태를 갱신한다.
  function updateConsentState() {
    const checkboxes = Array.from(elements.consentCheckboxes);
    elements.consentAll.checked = checkboxes.every((checkbox) => checkbox.checked);
    elements.completeSignupButton.disabled = !authUtils.hasRequiredConsents(getConsentState());
    elements.consentError.textContent = "";
    elements.consentNotice.classList.remove("is-visible");
  }

  // 입력값: 전체 동의 체크박스 변경 이벤트
  // 출력값: 없음
  // 기능: 필수와 선택을 포함한 모든 동의 항목을 같은 상태로 변경한다.
  function toggleAllConsents(event) {
    elements.consentCheckboxes.forEach((checkbox) => {
      checkbox.checked = event.target.checked;
    });
    updateConsentState();
  }

  // 입력값: 약관 동의 폼 제출 이벤트
  // 출력값: 없음
  // 기능: 필수 약관을 검증하고 정적 회원가입 화면의 완료 상태를 안내한다.
  function handleConsentSubmit(event) {
    event.preventDefault();
    if (!authUtils.hasRequiredConsents(getConsentState())) {
      elements.consentError.textContent = "필수 약관 두 항목에 동의해 주세요.";
      return;
    }

    if (pendingSignupProfile) {
      saveProfile(pendingSignupProfile);
    }
    showDashboard(false, pendingSignupProfile?.name);
    pendingSignupProfile = null;
  }

  // 입력값: 비밀번호 보기 버튼
  // 출력값: 없음
  // 기능: 연결된 비밀번호 입력값의 표시 여부와 버튼 설명을 전환한다.
  function togglePasswordVisibility(button) {
    const passwordInput = document.querySelector(`#${button.dataset.passwordTarget}`);
    const shouldShow = passwordInput.type === "password";
    passwordInput.type = shouldShow ? "text" : "password";
    button.textContent = shouldShow ? "숨김" : "보기";
    button.setAttribute("aria-pressed", String(shouldShow));
    button.setAttribute("aria-label", shouldShow ? "비밀번호 숨기기" : "비밀번호 표시");
    passwordInput.focus();
  }

  // 입력값: 입력 이벤트
  // 출력값: 없음
  // 기능: 사용자가 값을 다시 입력하면 해당 필드의 이전 오류를 지운다.
  function clearInputError(event) {
    showFieldError(event.target.id, "");
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 화면 이동, 폼 제출, 입력과 동의 요소에 상호작용을 연결한다.
  function bindEvents() {
    document.addEventListener("click", (event) => {
      const viewButton = event.target.closest("[data-view-target]");
      const passwordButton = event.target.closest("[data-password-target]");
      const termButton = event.target.closest("[data-term]");

      if (viewButton) showView(viewButton.dataset.viewTarget);
      if (passwordButton) togglePasswordVisibility(passwordButton);
      if (termButton) showToast(`${termButton.dataset.term} 상세 화면은 현재 제작 범위에 포함되지 않습니다.`);
    });
    document.querySelectorAll("[data-dashboard-action]").forEach((button) => {
      button.addEventListener("click", () => showToast(`${button.dataset.dashboardAction} 기능은 메인 대시보드 다음 제작 범위입니다.`));
    });
    elements.backButton.addEventListener("click", () => {
      const previousView = viewHistory.pop() || (activeView === "consent" ? "signup" : "login");
      showView(previousView, false);
    });
    elements.loginForm.addEventListener("submit", handleLoginSubmit);
    elements.signupForm.addEventListener("submit", handleSignupSubmit);
    elements.consentForm.addEventListener("submit", handleConsentSubmit);
    elements.consentAll.addEventListener("change", toggleAllConsents);
    elements.consentCheckboxes.forEach((checkbox) => checkbox.addEventListener("change", updateConsentState));
    elements.previousMonthButton.addEventListener("click", () => moveCalendarMonth(-1));
    elements.nextMonthButton.addEventListener("click", () => moveCalendarMonth(1));
    elements.todayButton.addEventListener("click", showCurrentMonth);
    elements.forgotPasswordButton.addEventListener("click", () => showToast("비밀번호 찾기는 현재 제작 범위에 포함되지 않습니다."));
    elements.logoutButton.addEventListener("click", showLoginFromDashboard);
    [elements.loginId, elements.loginPassword, elements.signupName, elements.signupId, elements.signupEmail, elements.signupPassword, elements.signupPasswordConfirm].forEach((input) => {
      input.addEventListener("input", clearInputError);
    });
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 주소의 해시 링크를 읽어 요청한 정적 화면을 직접 연다.
  function openLinkedView() {
    const linkedView = window.location.hash.replace("#", "");
    if (linkedView === "dashboard") {
      showDashboard(false, readStoredProfile()?.name);
      return;
    }

    if (viewMeta[linkedView]) {
      elements.dashboardView.classList.add("is-hidden");
      elements.siteHeader.classList.remove("is-hidden");
      elements.authContent.classList.remove("is-hidden");
      elements.siteFooter.classList.remove("is-hidden");
      if (linkedView !== activeView) {
        showView(linkedView, false);
      } else {
        document.title = viewMeta[linkedView].title;
        elements.authContent.focus({ preventScroll: true });
      }
    }
  }

  // 입력값: 없음
  // 출력값: 없음
  // 기능: 연도, 오류 상태와 이벤트를 준비해 인증 흐름을 시작한다.
  function initialize() {
    elements.currentYear.textContent = String(new Date().getFullYear());
    ["loginId", "loginPassword", "signupName", "signupId", "signupEmail", "signupPassword", "signupPasswordConfirm"].forEach((fieldId) => {
      elements[fieldId].setAttribute("aria-invalid", "false");
    });
    bindEvents();
    updateConsentState();
    openLinkedView();
    window.addEventListener("hashchange", openLinkedView);
  }

  initialize();
})();
