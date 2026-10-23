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
  selectedWeekday: "월",
  timetableSettingsTab: "subjects",
  classEditorOpen: false,
  editingClassId: null,
  classDraft: {
    day: "월",
    period: "1",
    title: "",
    teacher: "",
    room: "",
    color: "violet",
  },
  timetable: [
    { id: 1, day: "월", period: 1, title: "국어", teacher: "김선생님", room: "2학년 3반", color: "violet" },
    { id: 2, day: "월", period: 2, title: "수학", teacher: "박선생님", room: "2학년 3반", color: "blue" },
    { id: 3, day: "월", period: 3, title: "영어", teacher: "이선생님", room: "영어전용실", color: "green" },
    { id: 4, day: "월", period: 4, title: "과학", teacher: "최선생님", room: "과학실", color: "orange" },
    { id: 5, day: "화", period: 1, title: "수학", teacher: "박선생님", room: "2학년 3반", color: "blue" },
    { id: 6, day: "화", period: 2, title: "과학", teacher: "최선생님", room: "과학실", color: "orange" },
    { id: 7, day: "화", period: 3, title: "한국사", teacher: "정선생님", room: "2학년 3반", color: "pink" },
    { id: 8, day: "화", period: 5, title: "영어", teacher: "이선생님", room: "영어전용실", color: "green" },
    { id: 9, day: "수", period: 1, title: "영어", teacher: "이선생님", room: "영어전용실", color: "green" },
    { id: 10, day: "수", period: 2, title: "국어", teacher: "김선생님", room: "2학년 3반", color: "violet" },
    { id: 11, day: "수", period: 3, title: "수학", teacher: "박선생님", room: "2학년 3반", color: "blue" },
    { id: 12, day: "목", period: 1, title: "사회", teacher: "한선생님", room: "2학년 3반", color: "yellow" },
    { id: 13, day: "목", period: 2, title: "과학", teacher: "최선생님", room: "과학실", color: "orange" },
    { id: 14, day: "금", period: 1, title: "국어", teacher: "김선생님", room: "2학년 3반", color: "violet" },
    { id: 15, day: "금", period: 2, title: "수학", teacher: "박선생님", room: "2학년 3반", color: "blue" },
  ],
  periodTimes: [
    { period: 1, start: "09:00", end: "09:50" },
    { period: 2, start: "10:00", end: "10:50" },
    { period: 3, start: "11:00", end: "11:50" },
    { period: 4, start: "12:00", end: "12:50" },
    { period: 5, start: "14:00", end: "14:50" },
    { period: 6, start: "15:00", end: "15:50" },
    { period: 7, start: "16:00", end: "16:50" },
  ],
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
  gear: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="3" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M19.2 13.3a7.5 7.5 0 0 0 0-2.6l2-1.6-2-3.4-2.5 1a8.8 8.8 0 0 0-2.2-1.3L14.2 3h-4.4l-.4 2.4a8.8 8.8 0 0 0-2.2 1.3l-2.4-1-2.1 3.4 2.1 1.6a7.5 7.5 0 0 0 0 2.6l-2.1 1.6 2.1 3.4 2.4-1a8.8 8.8 0 0 0 2.2 1.3l.4 2.4h4.4l.4-2.4a8.8 8.8 0 0 0 2.2-1.3l2.4 1 2.1-3.4-2.1-1.6Z" fill="none" stroke="currentColor" stroke-width="1.4" stroke-linejoin="round"/></svg>`,
  trash: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 7h14M9 7V4h6v3M7 7l1 13h8l1-13M10 11v5M14 11v5" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
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

// 입력값: 현재 활성화할 메뉴 이름
// 출력값: 공통 하단 내비게이션 HTML 문자열
// 기능: 홈, 시간표, 캘린더, 마이 화면을 오가는 고정 메뉴를 표시한다.
function renderBottomNav(activeView) {
  const navItems = [
    ["home", "홈", "home"],
    ["timetable", "시간표", "timetable"],
    ["calendar", "캘린더", "calendar"],
    ["my", "마이", "user"],
  ];

  return `<nav class="bottom-nav" aria-label="주요 메뉴">${navItems
    .map(([view, label, iconName]) => `<button class="nav-item ${activeView === view ? "is-active" : ""}" type="button" data-action="navigate" data-view="${view}">${icon(iconName)}<span>${label}</span></button>`)
    .join("")}</nav>`;
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

      ${state.addMenuOpen ? `<div class="menu-scrim" data-action="close-add-menu"></div><div class="quick-menu"><button type="button" data-action="navigate" data-view="upload">${icon("image")}<span>사진으로 추가</span></button><button type="button" data-action="new-event">${icon("pencil")}<span>직접 입력</span></button></div>` : ""}

      ${renderBottomNav("home")}
    </section>
  `;
}

// 입력값: 수업 정보 또는 빈 값
// 출력값: 없음
// 기능: 새 수업 또는 기존 수업의 값을 수업 편집 상태에 준비한다.
function openClassEditor(classItem = null) {
  state.editingClassId = classItem?.id ?? null;
  state.classDraft = classItem
    ? { day: classItem.day, period: String(classItem.period), title: classItem.title, teacher: classItem.teacher, room: classItem.room, color: classItem.color }
    : { day: state.selectedWeekday, period: "1", title: "", teacher: "", room: "", color: "violet" };
  state.classEditorOpen = true;
}

// 입력값: 없음
// 출력값: 수업 편집 바텀시트 HTML 문자열
// 기능: 과목명, 교시, 교사, 교실과 색상을 입력해 수업을 추가하거나 수정한다.
function renderClassEditor() {
  if (!state.classEditorOpen) return "";
  const weekdays = ["월", "화", "수", "목", "금"];
  const colors = ["violet", "blue", "green", "orange", "pink", "yellow"];

  return `
    <div class="sheet-scrim" data-action="close-class-editor"></div>
    <section class="bottom-sheet class-editor-sheet" aria-modal="true" role="dialog">
      <div class="sheet-handle"></div>
      <div class="sheet-title-row"><h2>${state.editingClassId ? "수업 수정" : "수업 추가"}</h2><button type="button" data-action="close-class-editor" aria-label="닫기">${icon("close")}</button></div>
      <form class="class-form" data-form="class" novalidate>
        <label class="compact-field"><span>과목명</span><input name="title" value="${escapeAttribute(state.classDraft.title)}" placeholder="과목명을 입력해 주세요" /></label>
        <div class="field-pair">
          <label class="compact-field"><span>요일</span><select name="day">${weekdays.map((day) => `<option ${day === state.classDraft.day ? "selected" : ""}>${day}</option>`).join("")}</select></label>
          <label class="compact-field"><span>교시</span><select name="period">${state.periodTimes.map(({ period }) => `<option value="${period}" ${String(period) === state.classDraft.period ? "selected" : ""}>${period}교시</option>`).join("")}</select></label>
        </div>
        <div class="field-pair">
          <label class="compact-field"><span>선생님</span><input name="teacher" value="${escapeAttribute(state.classDraft.teacher)}" placeholder="선생님" /></label>
          <label class="compact-field"><span>교실</span><input name="room" value="${escapeAttribute(state.classDraft.room)}" placeholder="교실" /></label>
        </div>
        <fieldset class="color-field"><legend>색상</legend><div>${colors.map((color) => `<button class="color-choice color-${color} ${color === state.classDraft.color ? "is-selected" : ""}" type="button" data-action="choose-class-color" data-color="${color}" aria-label="${color} 색상"></button>`).join("")}</div></fieldset>
        <button class="primary-button" type="submit">${state.editingClassId ? "수정 완료" : "추가하기"}</button>
      </form>
    </section>`;
}

// 입력값: 없음
// 출력값: 선택한 요일의 수업 카드 HTML 문자열
// 기능: 시간 순서대로 수업을 표시하고 비어 있을 때 안내 문구를 보여 준다.
function renderTimetableClasses() {
  const classes = state.timetable.filter((item) => item.day === state.selectedWeekday).sort((a, b) => a.period - b.period);
  if (classes.length === 0) {
    return `<div class="timetable-empty">${icon("timetable")}<strong>등록된 수업이 없어요</strong><p>상단의 설정에서 시간표를 등록해 주세요</p><button type="button" data-action="open-timetable-settings">시간표 설정</button></div>`;
  }

  return classes.map((item) => {
    const period = state.periodTimes.find((entry) => entry.period === item.period);
    return `<button class="class-row" type="button" data-action="edit-class" data-class-id="${item.id}"><span class="period-label"><strong>${item.period}</strong><small>${period?.start ?? ""}</small></span><span class="class-card color-${item.color}"><strong>${item.title}</strong><small>${item.teacher} · ${item.room}</small></span></button>`;
  }).join("");
}

// 입력값: 없음
// 출력값: 내 시간표 화면 HTML 문자열
// 기능: 요일별 수업 목록과 시간표 설정 진입 기능을 표시한다.
function renderTimetable() {
  const weekdays = ["월", "화", "수", "목", "금"];
  return `
    <section class="screen timetable-screen">
      <header class="feature-header"><h1>내 시간표</h1><button type="button" data-action="open-timetable-settings" aria-label="시간표 설정">${icon("gear")}</button></header>
      <div class="weekday-tabs">${weekdays.map((day) => `<button class="${state.selectedWeekday === day ? "is-active" : ""}" type="button" data-action="select-weekday" data-day="${day}">${day}</button>`).join("")}</div>
      <div class="timetable-list">${renderTimetableClasses()}</div>
      <button class="floating-button timetable-add" type="button" data-action="add-class" aria-label="수업 추가">${icon("plus")}</button>
      ${renderBottomNav("timetable")}
      ${renderClassEditor()}
    </section>`;
}

// 입력값: 없음
// 출력값: 요일별 과목 설정 HTML 문자열
// 기능: 등록한 수업을 요일별로 확인하고 수정·삭제할 수 있게 표시한다.
function renderSubjectSettings() {
  const weekdays = ["월", "화", "수", "목", "금"];
  return `<div class="settings-content">${weekdays.map((day) => {
    const classes = state.timetable.filter((item) => item.day === day).sort((a, b) => a.period - b.period);
    return `<section class="settings-day"><h3>${day}요일</h3>${classes.length ? classes.map((item) => `<div class="settings-class color-${item.color}"><button type="button" data-action="edit-class" data-class-id="${item.id}"><strong>${item.period}교시 · ${item.title}</strong><small>${item.teacher} · ${item.room}</small></button><button class="row-delete" type="button" data-action="delete-class" data-class-id="${item.id}" aria-label="${item.title} 삭제">${icon("trash")}</button></div>`).join("") : `<p class="settings-empty">등록된 수업이 없어요</p>`}<button class="inline-add" type="button" data-action="add-class-for-day" data-day="${day}">${icon("plus")} 과목 추가</button></section>`;
  }).join("")}</div>`;
}

// 입력값: 없음
// 출력값: 교시 시간 설정 HTML 문자열
// 기능: 각 교시의 시작·종료 시간을 수정 가능한 입력란으로 표시한다.
function renderPeriodSettings() {
  return `<div class="settings-content period-settings">${state.periodTimes.map(({ period, start, end }) => `<div class="period-setting-row"><strong>${period}교시</strong><label><span>시작</span><input type="time" data-period="${period}" data-time-field="start" value="${start}" /></label><i>—</i><label><span>종료</span><input type="time" data-period="${period}" data-time-field="end" value="${end}" /></label></div>`).join("")}<p class="settings-note">수정한 시간은 시간표와 오늘 일정에 바로 반영돼요.</p></div>`;
}

// 입력값: 없음
// 출력값: 시간표 설정 화면 HTML 문자열
// 기능: 요일별 과목과 교시 시간을 나누어 관리한다.
function renderTimetableSettings() {
  return `
    <section class="screen settings-screen">
      <header class="feature-header centered"><button type="button" data-action="navigate" data-view="timetable" aria-label="시간표로 돌아가기">${icon("back")}</button><h1>시간표 설정</h1><span></span></header>
      <div class="segment-control"><button class="${state.timetableSettingsTab === "subjects" ? "is-active" : ""}" type="button" data-action="set-timetable-tab" data-tab="subjects">요일별 과목</button><button class="${state.timetableSettingsTab === "periods" ? "is-active" : ""}" type="button" data-action="set-timetable-tab" data-tab="periods">교시 시간</button></div>
      ${state.timetableSettingsTab === "subjects" ? renderSubjectSettings() : renderPeriodSettings()}
      ${renderClassEditor()}
    </section>`;
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
    timetable: renderTimetable,
    timetableSettings: renderTimetableSettings,
  };
  app.innerHTML = (views[state.currentView] ?? renderHome)();
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
  const input = event.target.closest("input, select, textarea");
  const form = event.target.closest("form");
  if (!input) return;

  if (input.dataset.timeField) {
    const period = state.periodTimes.find((entry) => entry.period === Number(input.dataset.period));
    if (period) period[input.dataset.timeField] = input.value;
    return;
  }

  if (!form) return;

  if (form.dataset.form === "login") {
    state.login[input.name] = input.value;
    syncLoginForm(form);
  }

  if (form.dataset.form === "signup") {
    state.signup[input.name] = input.value;
    syncSignupForm(form);
  }

  if (form.dataset.form === "class") {
    state.classDraft[input.name] = input.value;
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


  if (form.dataset.form === "class" && state.classDraft.title.trim()) {
    if (state.editingClassId) {
      const classItem = state.timetable.find((item) => item.id === state.editingClassId);
      Object.assign(classItem, state.classDraft, { period: Number(state.classDraft.period) });
    } else {
      state.timetable.push({ id: Date.now(), ...state.classDraft, period: Number(state.classDraft.period) });
    }
    state.selectedWeekday = state.classDraft.day;
    state.classEditorOpen = false;
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

  if (action === "navigate") {
    state.currentView = target.dataset.view;
    state.addMenuOpen = false;
    state.classEditorOpen = false;
  }
  if (action === "select-weekday") state.selectedWeekday = target.dataset.day;
  if (action === "open-timetable-settings") state.currentView = "timetableSettings";
  if (action === "set-timetable-tab") state.timetableSettingsTab = target.dataset.tab;
  if (action === "add-class") openClassEditor();
  if (action === "add-class-for-day") {
    state.selectedWeekday = target.dataset.day;
    openClassEditor();
  }
  if (action === "edit-class") {
    const classItem = state.timetable.find((item) => item.id === Number(target.dataset.classId));
    if (classItem) openClassEditor(classItem);
  }
  if (action === "delete-class") {
    state.timetable = state.timetable.filter((item) => item.id !== Number(target.dataset.classId));
  }
  if (action === "close-class-editor") state.classEditorOpen = false;
  if (action === "choose-class-color") state.classDraft.color = target.dataset.color;

  renderApp();
}

app.addEventListener("input", handleInput);
app.addEventListener("submit", handleSubmit);
app.addEventListener("click", handleClick);
renderApp();
