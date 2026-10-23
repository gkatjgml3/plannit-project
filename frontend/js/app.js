const app = document.querySelector("#app");

const state = {
  currentView: "login",
  login: {
    email: "",
    password: "",
  },
  signup: {
    name: "",
    email: "",
    password: "",
    passwordConfirm: "",
  },
  passwordVisibility: {
    login: false,
    signup: false,
    passwordConfirm: false,
  },
  terms: {
    service: false,
    privacy: false,
    notification: false,
  },
  selectedDate: "23",
  homeFilter: "all",
  addMenuOpen: false,
};

const homeContentByDate = {
  "18": {
    heading: "10월 18일 일요일",
    deadlines: [],
    schedules: [],
  },
  "19": {
    heading: "10월 19일 월요일",
    deadlines: [],
    schedules: [
      { time: "09:00", title: "국어", meta: "1교시 · 3반 교실", type: "class" },
      { time: "10:00", title: "수학", meta: "2교시 · 3반 교실", type: "class" },
      { time: "11:00", title: "영어", meta: "3교시 · 3반 교실", type: "class" },
      { time: "12:00", title: "과학", meta: "4교시 · 과학실", type: "class" },
    ],
  },
  "20": {
    heading: "10월 20일 화요일",
    deadlines: [],
    schedules: [
      { time: "09:00", title: "수학", meta: "1교시 · 3반 교실", type: "class" },
      { time: "10:00", title: "과학", meta: "2교시 · 과학실", type: "class" },
      { time: "11:00", title: "한국사", meta: "3교시 · 3반 교실", type: "class" },
      { time: "14:00", title: "영어", meta: "5교시 · 3반 교실", type: "class" },
    ],
  },
  "21": {
    heading: "10월 21일 수요일",
    deadlines: [],
    schedules: [
      { time: "09:00", title: "영어", meta: "1교시 · 3반 교실", type: "class" },
      { time: "10:00", title: "국어", meta: "2교시 · 3반 교실", type: "class" },
      { time: "11:00", title: "수학", meta: "3교시 · 3반 교실", type: "class" },
      { time: "14:00", title: "과학", meta: "5교시 · 과학실", type: "class" },
    ],
  },
  "22": {
    heading: "10월 22일 목요일",
    deadlines: [],
    schedules: [
      { time: "09:00", title: "사회", meta: "1교시 · 3반 교실", type: "class" },
      { time: "10:00", title: "과학", meta: "2교시 · 과학실", type: "class" },
      { time: "11:00", title: "수학", meta: "3교시 · 3반 교실", type: "class" },
      { time: "14:00", title: "한국사", meta: "5교시 · 3반 교실", type: "class" },
    ],
  },
  "23": {
    heading: "10월 23일 금요일",
    deadlines: [
      { title: "영어 수행평가 제출", due: "23:59까지" },
      { title: "수학 수행 보고서", due: "종례 전까지" },
    ],
    schedules: [
      { time: "09:00", title: "국어", meta: "1교시 · 3반 교실", type: "class" },
      { time: "10:00", title: "수학", meta: "2교시 · 3반 교실", type: "class" },
      { time: "11:00", title: "영어", meta: "3교시 · 3반 교실", type: "class" },
      { time: "12:00", title: "사회", meta: "4교시 · 3반 교실", type: "class" },
      { time: "14:00", title: "과학 조별 발표", meta: "과학실 · 2학년 3반", type: "personal" },
    ],
  },
  "24": {
    heading: "10월 24일 토요일",
    deadlines: [],
    schedules: [],
  },
};

const icons = {
  back: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14.5 5-7 7 7 7" fill="none" stroke="currentColor" stroke-width="1.9" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  eye: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M3.5 12s3.1-5 8.5-5 8.5 5 8.5 5-3.1 5-8.5 5-8.5-5-8.5-5Z" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="12" cy="12" r="2.4" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>`,
  chevron: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 5 7 7-7 7" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  check: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4 4 10-10" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  minus: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 12h12" fill="none" stroke="currentColor" stroke-width="2.2" stroke-linecap="round"/></svg>`,
  bell: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 17h12l-1.2-1.8V10a4.8 4.8 0 0 0-9.6 0v5.2L6 17Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="M10 20h4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  plus: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>`,
  close: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m6 6 12 12M18 6 6 18" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>`,
  image: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="9" cy="9" r="1.4" fill="none" stroke="currentColor" stroke-width="1.6"/><path d="m5 17 4.5-4 3 2.5 2-2 4.5 4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  pencil: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M5 19l3.5-.8L18 8.7a2.1 2.1 0 0 0-3-3L5.5 15.2 5 19Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  home: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m4 10 8-6 8 6v9a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  timetable: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M9 4v16M4 9h16M4 14h16" fill="none" stroke="currentColor" stroke-width="1.4"/></svg>`,
  calendar: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="15" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M8 3v4M16 3v4M4 9h16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  user: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3.2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M5 20c.7-3.2 3.1-5 7-5s6.3 1.8 7 5" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
};

// 입력값: 아이콘 이름
// 출력값: 화면에 삽입할 SVG 문자열
// 기능: 피그마 화면에 사용된 선형 아이콘을 반환한다.
function icon(name) {
  return icons[name] ?? "";
}

// 입력값: 사용자 입력 문자열
// 출력값: HTML 속성에 안전하게 넣을 문자열
// 기능: 화면을 다시 그릴 때 입력값이 마크업으로 해석되지 않도록 변환한다.
function escapeAttribute(value) {
  return String(value)
    .replaceAll("&", "&amp;")
    .replaceAll('"', "&quot;")
    .replaceAll("<", "&lt;")
    .replaceAll(">", "&gt;");
}

// 입력값: 없음
// 출력값: 로그인 화면 HTML 문자열
// 기능: PLANIT 첫 진입 로그인 화면을 피그마 구성에 맞춰 표시한다.
function renderLogin() {
  const passwordType = state.passwordVisibility.login ? "text" : "password";

  return `
    <section class="screen auth-screen login-screen">
      <div class="brand-block">
        <p class="brand-name">PLANIT</p>
        <h1>학교 수업부터 내 할 일까지,<br />오늘 하루를 한눈에.</h1>
      </div>

      <form class="auth-form login-form" data-form="login" novalidate>
        <label class="form-field">
          <span>이메일</span>
          <input name="email" type="email" autocomplete="email" placeholder="example@email.com" value="${escapeAttribute(state.login.email)}" />
        </label>
        <label class="form-field">
          <span>비밀번호</span>
          <span class="input-wrap">
            <input name="password" type="${passwordType}" autocomplete="current-password" placeholder="비밀번호 입력" value="${escapeAttribute(state.login.password)}" />
            <button class="input-icon" type="button" data-action="toggle-password" data-password-field="login" aria-label="비밀번호 표시 전환">${icon("eye")}</button>
          </span>
        </label>
        <button class="primary-button" type="submit" data-submit="login" ${state.login.email && state.login.password ? "" : "disabled"}>로그인</button>
        <button class="forgot-button" type="button">비밀번호를 잊으셨나요?</button>
      </form>

      <p class="auth-footer">아직 계정이 없나요?<button type="button" data-action="open-signup">회원가입</button></p>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 회원가입 화면 HTML 문자열
// 기능: 이름, 이메일, 비밀번호를 입력하는 가입 정보 화면을 표시한다.
function renderSignup() {
  const passwordType = state.passwordVisibility.signup ? "text" : "password";
  const confirmType = state.passwordVisibility.passwordConfirm ? "text" : "password";

  return `
    <section class="screen signup-screen">
      <header class="top-header">
        <button class="back-button" type="button" data-action="back-login" aria-label="로그인으로 돌아가기">${icon("back")}</button>
        <h1>회원가입</h1>
        <span></span>
      </header>

      <div class="signup-copy">
        <h2>가입 정보를 입력해 주세요</h2>
        <p>고등학생 기준으로 시작해요</p>
      </div>

      <form class="auth-form signup-form" data-form="signup" novalidate>
        <label class="form-field"><span>이름</span><input name="name" type="text" autocomplete="name" placeholder="이름 입력" value="${escapeAttribute(state.signup.name)}" /></label>
        <label class="form-field"><span>이메일</span><input name="email" type="email" autocomplete="email" placeholder="example@email.com" value="${escapeAttribute(state.signup.email)}" /></label>
        <label class="form-field">
          <span>비밀번호</span>
          <span class="input-wrap"><input name="password" type="${passwordType}" autocomplete="new-password" placeholder="비밀번호 입력" value="${escapeAttribute(state.signup.password)}" /><button class="input-icon" type="button" data-action="toggle-password" data-password-field="signup" aria-label="비밀번호 표시 전환">${icon("eye")}</button></span>
          <small data-password-guide>영문, 숫자를 포함해 8자 이상</small>
        </label>
        <label class="form-field">
          <span>비밀번호 확인</span>
          <span class="input-wrap"><input name="passwordConfirm" type="${confirmType}" autocomplete="new-password" placeholder="비밀번호 다시 입력" value="${escapeAttribute(state.signup.passwordConfirm)}" /><button class="input-icon" type="button" data-action="toggle-password" data-password-field="passwordConfirm" aria-label="비밀번호 확인 표시 전환">${icon("eye")}</button></span>
          <small class="field-error" data-confirm-error hidden>비밀번호가 일치하지 않아요</small>
        </label>
        <button class="primary-button signup-submit" type="submit" data-submit="signup" ${isSignupValid() ? "" : "disabled"}>다음</button>
      </form>
    </section>
  `;
}

// 입력값: 약관 선택 여부, 전체 선택 부분 상태 여부
// 출력값: 약관 체크박스 내부 아이콘 HTML 문자열
// 기능: 체크, 부분 선택, 미선택 상태를 구분해 표시한다.
function renderTermCheckbox(isChecked, isPartial = false) {
  const className = isChecked || isPartial ? "term-checkbox is-checked" : "term-checkbox";
  const checkedState = isPartial ? "mixed" : String(isChecked);
  return `<span class="${className}" role="checkbox" aria-checked="${checkedState}">${isPartial ? icon("minus") : isChecked ? icon("check") : ""}</span>`;
}

// 입력값: 없음
// 출력값: 약관 동의 화면 HTML 문자열
// 기능: 필수 및 선택 약관을 확인하고 가입을 완료하는 화면을 표시한다.
function renderTerms() {
  const selectedCount = Object.values(state.terms).filter(Boolean).length;
  const allChecked = selectedCount === 3;
  const partiallyChecked = selectedCount > 0 && !allChecked;
  const requiredChecked = state.terms.service && state.terms.privacy;

  return `
    <section class="screen terms-screen">
      <header class="top-header">
        <button class="back-button" type="button" data-action="back-signup" aria-label="회원가입으로 돌아가기">${icon("back")}</button>
        <h1>약관 동의</h1>
        <span></span>
      </header>

      <h2>PLANIT 이용을 위해<br />약관에 동의해 주세요</h2>

      <div class="terms-list">
        <button class="all-terms" type="button" data-action="toggle-all-terms">${renderTermCheckbox(allChecked, partiallyChecked)}<strong>전체 동의</strong></button>

        <div class="term-row">
          <button class="term-main" type="button" data-action="toggle-term" data-term="service">${renderTermCheckbox(state.terms.service)}<span><strong>[필수]</strong> 서비스 이용약관</span></button>
          <button class="term-detail" type="button" aria-label="서비스 이용약관 상세">${icon("chevron")}</button>
        </div>
        <div class="term-row">
          <button class="term-main" type="button" data-action="toggle-term" data-term="privacy">${renderTermCheckbox(state.terms.privacy)}<span><strong>[필수]</strong> 개인정보 수집·이용 동의</span></button>
          <button class="term-detail" type="button" aria-label="개인정보 수집 이용 동의 상세">${icon("chevron")}</button>
        </div>
        <div class="term-row">
          <button class="term-main" type="button" data-action="toggle-term" data-term="notification">${renderTermCheckbox(state.terms.notification)}<span><b>[선택]</b> 일정 알림 수신 동의</span></button>
        </div>
      </div>

      <button class="primary-button terms-submit" type="button" data-action="complete-signup" ${requiredChecked ? "" : "disabled"}>가입 완료</button>
    </section>
  `;
}

// 입력값: 선택한 날짜의 일정 목록과 현재 필터 이름
// 출력값: 선택된 필터에 해당하는 일정 카드 HTML 문자열
// 기능: 메인 홈의 전체, 수업, 개인 일정 목록을 구분해 반환한다.
function renderScheduleItems(schedules, filter) {
  const filteredSchedules = filter === "all" ? schedules : schedules.filter((schedule) => schedule.type === filter);

  if (filteredSchedules.length === 0) {
    const emptyMessage = filter === "personal" ? "등록된 개인 일정이 없어요" : "등록된 일정이 없어요";
    return `<p class="schedule-empty">${emptyMessage}</p>`;
  }

  return filteredSchedules
    .map((schedule) => `<article class="schedule-row"><time>${schedule.time}</time><div class="schedule-card"><strong>${schedule.title}</strong><span>${schedule.meta}</span></div></article>`)
    .join("");
}

// 입력값: 선택한 날짜의 마감 일정 목록
// 출력값: 마감 일정 카드 HTML 문자열
// 기능: 선택한 날짜의 마감 일정 또는 빈 상태를 표시한다.
function renderDeadlines(deadlines) {
  if (deadlines.length === 0) {
    return `<div class="deadline-empty">마감 일정이 없어요</div>`;
  }

  return `<div class="deadline-card">${deadlines.map((deadline) => `<p><strong>${deadline.title}</strong><span>${deadline.due}</span></p>`).join("")}</div>`;
}

// 입력값: 없음
// 출력값: 메인 홈 화면 HTML 문자열
// 기능: 마감 일정, 오늘의 시간표, 빠른 추가 메뉴를 피그마 화면대로 표시한다.
function renderHome() {
  const filters = [
    ["all", "전체"],
    ["class", "수업"],
    ["personal", "개인"],
  ];
  const selectedContent = homeContentByDate[state.selectedDate];
  const dates = [["일", "18"], ["월", "19"], ["화", "20"], ["수", "21"], ["목", "22"], ["금", "23"], ["토", "24"]];

  return `
    <section class="screen home-screen ${state.addMenuOpen ? "has-menu" : ""}">
      <div class="home-scroll">
        <header class="home-header"><h1>${selectedContent.heading}</h1><button class="bell-button" type="button" aria-label="알림">${icon("bell")}</button></header>

        <div class="date-strip" aria-label="10월 셋째 주">
          ${dates.map(([day, date]) => `<button class="date-item ${date === state.selectedDate ? "is-selected" : ""} ${day === "일" ? "is-sunday" : ""}" type="button" data-action="select-date" data-date="${date}" aria-pressed="${date === state.selectedDate}"><small>${day}</small><strong>${date}</strong></button>`).join("")}
        </div>

        <section class="deadline-section">
          <h2>${state.selectedDate === "23" ? "오늘 마감" : "마감 일정"} <em>${selectedContent.deadlines.length}</em></h2>
          ${renderDeadlines(selectedContent.deadlines)}
        </section>

        <section class="schedule-section">
          <div class="schedule-heading"><h2>${state.selectedDate === "23" ? "오늘의 일정" : "일정"}</h2><div class="schedule-filters">${filters.map(([value, label]) => `<button class="${state.homeFilter === value ? "is-active" : ""}" type="button" data-action="set-filter" data-filter="${value}">${label}</button>`).join("")}</div></div>
          <div class="schedule-list">${renderScheduleItems(selectedContent.schedules, state.homeFilter)}</div>
        </section>
      </div>

      <button class="floating-button" type="button" data-action="toggle-add-menu" aria-label="일정 추가 메뉴 ${state.addMenuOpen ? "닫기" : "열기"}">${state.addMenuOpen ? icon("close") : icon("plus")}</button>

      ${state.addMenuOpen ? `<div class="menu-scrim" data-action="close-add-menu"></div><div class="quick-menu"><button type="button">${icon("image")}<span>사진으로 추가</span></button><button type="button">${icon("pencil")}<span>직접 입력</span></button></div>` : ""}

      <nav class="bottom-nav" aria-label="주요 메뉴">
        <button class="nav-item is-active" type="button">${icon("home")}<span>홈</span></button>
        <button class="nav-item" type="button">${icon("timetable")}<span>시간표</span></button>
        <button class="nav-item" type="button">${icon("calendar")}<span>캘린더</span></button>
        <button class="nav-item" type="button">${icon("user")}<span>마이</span></button>
      </nav>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 회원가입 입력값의 유효 여부
// 기능: 이름, 이메일, 비밀번호 규칙과 비밀번호 일치 여부를 검사한다.
function isSignupValid() {
  const hasValidEmail = /^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(state.signup.email);
  const hasValidPassword = /[A-Za-z]/.test(state.signup.password) && /\d/.test(state.signup.password) && state.signup.password.length >= 8;
  return Boolean(state.signup.name.trim() && hasValidEmail && hasValidPassword && state.signup.password === state.signup.passwordConfirm);
}

// 입력값: 없음
// 출력값: 없음
// 기능: 현재 화면 상태에 맞는 화면을 앱 프레임 안에 렌더링한다.
function renderApp() {
  const views = {
    login: renderLogin,
    signup: renderSignup,
    terms: renderTerms,
    home: renderHome,
  };
  app.innerHTML = views[state.currentView]();
}

// 입력값: 로그인 폼 요소
// 출력값: 없음
// 기능: 로그인 입력 여부에 따라 로그인 버튼의 활성 상태를 갱신한다.
function syncLoginForm(form) {
  const submitButton = form.querySelector('[data-submit="login"]');
  submitButton.disabled = !(state.login.email.trim() && state.login.password);
}

// 입력값: 회원가입 폼 요소
// 출력값: 없음
// 기능: 회원가입 값의 유효성을 표시하고 다음 버튼의 활성 상태를 갱신한다.
function syncSignupForm(form) {
  const submitButton = form.querySelector('[data-submit="signup"]');
  const passwordGuide = form.querySelector("[data-password-guide]");
  const confirmError = form.querySelector("[data-confirm-error]");
  const hasValidPassword = /[A-Za-z]/.test(state.signup.password) && /\d/.test(state.signup.password) && state.signup.password.length >= 8;
  const hasConfirmMismatch = Boolean(state.signup.passwordConfirm && state.signup.password !== state.signup.passwordConfirm);

  passwordGuide.classList.toggle("is-valid", hasValidPassword);
  confirmError.hidden = !hasConfirmMismatch;
  submitButton.disabled = !isSignupValid();
}

// 입력값: 폼 입력 이벤트
// 출력값: 없음
// 기능: 로그인과 회원가입 입력값을 상태에 저장하고 버튼 상태를 즉시 갱신한다.
function handleInput(event) {
  const input = event.target.closest("input");
  const form = event.target.closest("form");
  if (!input || !form) return;

  if (form.dataset.form === "login") {
    state.login[input.name] = input.value;
    syncLoginForm(form);
  }

  if (form.dataset.form === "signup") {
    state.signup[input.name] = input.value;
    syncSignupForm(form);
  }
}

// 입력값: 폼 제출 이벤트
// 출력값: 없음
// 기능: 유효한 로그인은 홈으로, 유효한 회원가입 정보는 약관 화면으로 이동시킨다.
function handleSubmit(event) {
  const form = event.target.closest("form");
  if (!form) return;
  event.preventDefault();

  if (form.dataset.form === "login" && state.login.email.trim() && state.login.password) {
    state.currentView = "home";
    renderApp();
  }

  if (form.dataset.form === "signup" && isSignupValid()) {
    state.currentView = "terms";
    renderApp();
  }
}

// 입력값: 화면 클릭 이벤트
// 출력값: 없음
// 기능: 화면 이동, 비밀번호 표시, 약관 선택, 홈 필터와 추가 메뉴 동작을 처리한다.
function handleClick(event) {
  const target = event.target.closest("[data-action]");
  if (!target) return;
  const { action } = target.dataset;

  if (action === "open-signup") state.currentView = "signup";
  if (action === "back-login") state.currentView = "login";
  if (action === "back-signup") state.currentView = "signup";

  if (action === "toggle-password") {
    const field = target.dataset.passwordField;
    state.passwordVisibility[field] = !state.passwordVisibility[field];
  }

  if (action === "toggle-term") {
    const term = target.dataset.term;
    state.terms[term] = !state.terms[term];
  }

  if (action === "toggle-all-terms") {
    const shouldSelectAll = !Object.values(state.terms).every(Boolean);
    Object.keys(state.terms).forEach((term) => {
      state.terms[term] = shouldSelectAll;
    });
  }

  if (action === "complete-signup" && state.terms.service && state.terms.privacy) {
    state.currentView = "home";
  }

  if (action === "set-filter") state.homeFilter = target.dataset.filter;
  if (action === "select-date") {
    state.selectedDate = target.dataset.date;
    state.homeFilter = "all";
  }
  if (action === "toggle-add-menu") state.addMenuOpen = !state.addMenuOpen;
  if (action === "close-add-menu") state.addMenuOpen = false;

  renderApp();
}

app.addEventListener("input", handleInput);
app.addEventListener("submit", handleSubmit);
app.addEventListener("click", handleClick);
renderApp();
