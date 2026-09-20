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
    scheduleDialog: document.querySelector("#scheduleDialog"),
    scheduleForm: document.querySelector("#scheduleForm"),
    scheduleTitle: document.querySelector("#scheduleTitle"),
    scheduleDate: document.querySelector("#scheduleDate"),
    closeScheduleDialogButton: document.querySelector("#closeScheduleDialogButton"),
    cancelScheduleButton: document.querySelector("#cancelScheduleButton"),
    scheduleDialogOpenButtons: document.querySelectorAll("[data-schedule-dialog-open]"),
    toast: document.querySelector("#toast"),
  };
  let activeView = "login";
  let currentCalendarDate = new Date(today.getFullYear(), today.getMonth(), 1);
  let shouldShowDemoSchedules = false;
  let pendingSignupProfile = null;
  let toastTimer = null;

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

  function saveProfile(profile) {
    try {
      window.localStorage.setItem(profileStorageKey, JSON.stringify(profile));
    } catch (error) {
      showToast("이름을 브라우저에 저장하지 못했습니다.");
    }
  }

  function findStoredProfile(loginId) {
    const profile = readStoredProfile();
    const normalizedLoginId = loginId.trim().toLowerCase();
    if (!profile) {
      return null;
    }
    return [profile.signupId, profile.email].some((value) => value.toLowerCase() === normalizedLoginId) ? profile : null;
  }

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

  function renderCalendar() {
    const year = currentCalendarDate.getFullYear();
    const monthIndex = currentCalendarDate.getMonth();
    const monthLabel = calendarUtils.createMonthLabel(year, monthIndex);
    const demoSchedules = shouldShowDemoSchedules ? getDemoSchedules() : [];

    elements.largeCalendarHeading.textContent = monthLabel;
    elements.calendarDays.setAttribute("aria-label", `${monthLabel} 월간 캘린더`);
    elements.calendarDays.replaceChildren();

    const calendarDays = calendarUtils.buildCalendarDays(year, monthIndex, today);
    const calendarWeekCount = calendarDays.length / 7;
    elements.calendarDays.style.setProperty("--calendar-week-count", String(calendarWeekCount));
    calendarDays.forEach((dayInfo) => {
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

  function moveCalendarMonth(amount) {
    currentCalendarDate = calendarUtils.moveMonth(currentCalendarDate, amount);
    renderCalendar();
  }

  function showCurrentMonth() {
    currentCalendarDate = new Date(today.getFullYear(), today.getMonth(), 1);
    renderCalendar();
  }

  function getTodayInputValue() {
    return `${today.getFullYear()}-${String(today.getMonth() + 1).padStart(2, "0")}-${String(today.getDate()).padStart(2, "0")}`;
  }

  function openScheduleDialog() {
    if (!elements.scheduleDate.value) {
      elements.scheduleDate.value = getTodayInputValue();
    }
    elements.scheduleDialog.showModal();
    elements.scheduleTitle.focus();
  }

  function closeScheduleDialog() {
    elements.scheduleDialog.close();
  }

  function handleScheduleSubmit(event) {
    event.preventDefault();
    if (!elements.scheduleForm.checkValidity()) {
      elements.scheduleForm.reportValidity();
      return;
    }
    closeScheduleDialog();
    elements.scheduleForm.reset();
    showToast("일정 추가 화면 동작을 확인했습니다. 실제 저장은 연결되지 않았습니다.");
  }

  function closeScheduleDialogFromBackdrop(event) {
    const dialogRect = elements.scheduleDialog.getBoundingClientRect();
    const isOutside = event.clientX < dialogRect.left || event.clientX > dialogRect.right || event.clientY < dialogRect.top || event.clientY > dialogRect.bottom;
    if (isOutside) {
      closeScheduleDialog();
    }
  }

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

  function showToast(message) {
    window.clearTimeout(toastTimer);
    elements.toast.textContent = message;
    elements.toast.classList.add("is-visible");
    toastTimer = window.setTimeout(() => elements.toast.classList.remove("is-visible"), 2400);
  }

  function showFieldError(fieldId, message) {
    const input = elements[fieldId];
    const errorElement = elements[`${fieldId}Error`];
    input.setAttribute("aria-invalid", String(Boolean(message)));
    errorElement.textContent = message || "";
  }

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

  function getConsentState() {
    return {
      serviceTerms: elements.consentForm.elements.serviceTerms.checked,
      privacyTerms: elements.consentForm.elements.privacyTerms.checked,
    };
  }

  function updateConsentState() {
    const checkboxes = Array.from(elements.consentCheckboxes);
    elements.consentAll.checked = checkboxes.every((checkbox) => checkbox.checked);
    elements.completeSignupButton.disabled = !authUtils.hasRequiredConsents(getConsentState());
    elements.consentError.textContent = "";
    elements.consentNotice.classList.remove("is-visible");
  }

  function toggleAllConsents(event) {
    elements.consentCheckboxes.forEach((checkbox) => {
      checkbox.checked = event.target.checked;
    });
    updateConsentState();
  }

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

  function togglePasswordVisibility(button) {
    const passwordInput = document.querySelector(`#${button.dataset.passwordTarget}`);
    const shouldShow = passwordInput.type === "password";
    passwordInput.type = shouldShow ? "text" : "password";
    button.textContent = shouldShow ? "숨김" : "보기";
    button.setAttribute("aria-pressed", String(shouldShow));
    button.setAttribute("aria-label", shouldShow ? "비밀번호 숨기기" : "비밀번호 표시");
    passwordInput.focus();
  }

  function clearInputError(event) {
    showFieldError(event.target.id, "");
  }

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
    elements.scheduleDialogOpenButtons.forEach((button) => button.addEventListener("click", openScheduleDialog));
    elements.closeScheduleDialogButton.addEventListener("click", closeScheduleDialog);
    elements.cancelScheduleButton.addEventListener("click", closeScheduleDialog);
    elements.scheduleForm.addEventListener("submit", handleScheduleSubmit);
    elements.scheduleDialog.addEventListener("click", closeScheduleDialogFromBackdrop);
    elements.forgotPasswordButton.addEventListener("click", () => showToast("비밀번호 찾기는 현재 제작 범위에 포함되지 않습니다."));
    elements.logoutButton.addEventListener("click", showLoginFromDashboard);
    [elements.loginId, elements.loginPassword, elements.signupName, elements.signupId, elements.signupEmail, elements.signupPassword, elements.signupPasswordConfirm].forEach((input) => {
      input.addEventListener("input", clearInputError);
    });
  }

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
