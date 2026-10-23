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
  calendarYear: 2026,
  calendarMonth: 10,
  calendarSelectedDay: 23,
  selectedEventId: null,
  eventFormMode: "create",
  eventReturnView: "calendar",
  eventDeleteOpen: false,
  dateTimePickerOpen: false,
  alarmPickerOpen: false,
  eventDraft: {
    title: "",
    category: "개인",
    date: "2026-10-23",
    start: "14:00",
    end: "15:00",
    allDay: false,
    location: "",
    memo: "",
    alarm: "10분 전",
  },
  events: [
    { id: 101, title: "영어 수행평가 제출", category: "과제", date: "2026-10-23", start: "23:00", end: "23:59", allDay: false, location: "온라인 클래스", memo: "영어 에세이 최종본 제출", alarm: "1시간 전", shared: false },
    { id: 102, title: "수학 수행 보고서", category: "과제", date: "2026-10-23", start: "08:30", end: "09:00", allDay: false, location: "2학년 3반", memo: "인쇄한 보고서 준비", alarm: "1일 전", shared: false },
    { id: 103, title: "과학 조별 발표", category: "그룹", date: "2026-10-23", start: "14:00", end: "15:00", allDay: false, location: "과학실", memo: "발표 자료와 실험 결과 준비", alarm: "30분 전", shared: true },
    { id: 104, title: "중간고사", category: "학교", date: "2026-10-15", start: "09:00", end: "12:00", allDay: false, location: "본교", memo: "국어, 수학 시험", alarm: "1일 전", shared: false },
    { id: 105, title: "동아리 회의", category: "그룹", date: "2026-10-28", start: "16:00", end: "17:00", allDay: false, location: "동아리실", memo: "축제 준비 회의", alarm: "10분 전", shared: true },
  ],
  upload: {
    status: "idle",
    fileName: "",
    fileSize: 0,
    previewUrl: "",
    error: "",
  },
  extractionCandidates: [],
  editingCandidateId: null,
  candidateDraft: null,
  registrationComplete: false,
  groupModalOpen: false,
  groupModalTab: "create",
  groupDraft: { name: "", code: "" },
  groupError: "",
  selectedGroupId: 1,
  groupShareOpen: false,
  selectedShareGroupIds: [],
  notificationEnabled: true,
  groups: [
    { id: 1, name: "2학년 3반", description: "우리 반 일정 공유", code: "PLN-23CLASS", color: "violet", members: ["김플래닛", "이하늘", "박민준", "최서윤", "정도윤"], eventIds: [103] },
    { id: 2, name: "과학 조별 과제", description: "과학 발표 준비 모임", code: "PLN-SCIENCE", color: "green", members: ["김플래닛", "이하늘", "박민준", "최서윤"], eventIds: [103] },
    { id: 3, name: "방송부", description: "방송부 공지와 활동 일정", code: "PLN-ON-AIR", color: "blue", members: ["김플래닛", "정도윤", "한유진"], eventIds: [105] },
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
  clock: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M12 7v5l3 2" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  pin: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 21s6-5.3 6-11a6 6 0 1 0-12 0c0 5.7 6 11 6 11Z" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="10" r="2" fill="none" stroke="currentColor" stroke-width="1.8"/></svg>`,
  note: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="4" width="14" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M8 9h8M8 13h8M8 17h5" fill="none" stroke="currentColor" stroke-width="1.6" stroke-linecap="round"/></svg>`,
  share: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="18" cy="5" r="2.2" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="6" cy="12" r="2.2" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="18" cy="19" r="2.2" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="m8 11 8-5M8 13l8 5" fill="none" stroke="currentColor" stroke-width="1.7"/></svg>`,
  upload: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 16V5M8 9l4-4 4 4M5 15v4h14v-4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  warning: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 4 3.5 19h17L12 4Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="M12 9v4M12 16.5v.1" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  refresh: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M19 8a8 8 0 1 0 .3 7M19 4v4h-4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  users: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="8" r="3" fill="none" stroke="currentColor" stroke-width="1.7"/><circle cx="17" cy="10" r="2.2" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="M3 20c.6-3.6 2.6-5.5 6-5.5s5.4 1.9 6 5.5M15 15c3.3 0 5.2 1.5 5.8 4" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>`,
  logout: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M10 5H5v14h5M14 8l4 4-4 4M8 12h10" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  link: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m10 14 4-4M8.5 16.5l-1 1a3.5 3.5 0 0 1-5-5l3-3a3.5 3.5 0 0 1 5 0M15.5 7.5l1-1a3.5 3.5 0 0 1 5 5l-3 3a3.5 3.5 0 0 1-5 0" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
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

// 입력값: 연도, 월, 일
// 출력값: YYYY-MM-DD 형식의 날짜 문자열
// 기능: 캘린더 셀과 일정 데이터가 공통으로 사용할 날짜 키를 만든다.
function toDateKey(year, month, day) {
  return `${year}-${String(month).padStart(2, "0")}-${String(day).padStart(2, "0")}`;
}

// 입력값: YYYY-MM-DD 형식의 날짜 문자열
// 출력값: 한국어 날짜 표시 문자열
// 기능: 일정 상세와 입력 화면에 읽기 쉬운 날짜를 표시한다.
function formatKoreanDate(dateKey) {
  const [year, month, day] = dateKey.split("-").map(Number);
  const weekday = ["일", "월", "화", "수", "목", "금", "토"][new Date(year, month - 1, day).getDay()];
  return `${year}년 ${month}월 ${day}일 ${weekday}요일`;
}

// 입력값: 일정 객체 또는 빈 값, 완료 뒤 돌아갈 화면
// 출력값: 없음
// 기능: 새 일정 또는 기존 일정을 일정 입력 상태에 준비하고 편집 화면으로 이동한다.
function openEventForm(eventItem = null, returnView = "calendar") {
  state.eventFormMode = eventItem ? "edit" : "create";
  state.eventReturnView = returnView;
  state.eventDraft = eventItem
    ? { title: eventItem.title, category: eventItem.category, date: eventItem.date, start: eventItem.start, end: eventItem.end, allDay: eventItem.allDay, location: eventItem.location, memo: eventItem.memo, alarm: eventItem.alarm }
    : { title: "", category: "개인", date: toDateKey(state.calendarYear, state.calendarMonth, state.calendarSelectedDay), start: "14:00", end: "15:00", allDay: false, location: "", memo: "", alarm: "10분 전" };
  state.dateTimePickerOpen = false;
  state.alarmPickerOpen = false;
  state.currentView = "eventForm";
}

// 입력값: 일정 종류
// 출력값: 카드에 적용할 종류별 클래스 이름
// 기능: 개인·과제·학교·그룹 일정을 색상으로 구분한다.
function getEventTone(category) {
  const tones = { 개인: "violet", 과제: "pink", 학교: "blue", 그룹: "green" };
  return tones[category] ?? "violet";
}

// 입력값: 없음
// 출력값: 현재 월의 달력 셀 HTML 문자열
// 기능: 이전·현재·다음 달 날짜를 포함한 6주 달력을 생성하고 일정 유무를 표시한다.
function renderCalendarGrid() {
  const firstDay = new Date(state.calendarYear, state.calendarMonth - 1, 1).getDay();
  const daysInMonth = new Date(state.calendarYear, state.calendarMonth, 0).getDate();
  const daysInPreviousMonth = new Date(state.calendarYear, state.calendarMonth - 1, 0).getDate();
  const cells = [];

  for (let index = 0; index < 42; index += 1) {
    let day = index - firstDay + 1;
    let monthOffset = 0;
    if (day < 1) {
      day = daysInPreviousMonth + day;
      monthOffset = -1;
    } else if (day > daysInMonth) {
      day -= daysInMonth;
      monthOffset = 1;
    }
    const cellDate = new Date(state.calendarYear, state.calendarMonth - 1 + monthOffset, day);
    const year = cellDate.getFullYear();
    const month = cellDate.getMonth() + 1;
    const dateKey = toDateKey(year, month, day);
    const hasEvents = state.events.some((item) => item.date === dateKey);
    const isSelected = dateKey === toDateKey(state.calendarYear, state.calendarMonth, state.calendarSelectedDay);
    const isCurrentMonth = monthOffset === 0;
    const weekday = index % 7;
    cells.push(`<button class="calendar-day ${isCurrentMonth ? "" : "is-outside"} ${isSelected ? "is-selected" : ""} ${weekday === 0 ? "is-sunday" : ""}" type="button" data-action="select-calendar-date" data-date="${dateKey}"><span>${day}</span>${hasEvents ? `<i></i>` : ""}</button>`);
  }
  return cells.join("");
}

// 입력값: 선택한 날짜의 일정 목록
// 출력값: 일정 요약 카드 HTML 문자열
// 기능: 선택한 날짜의 일정들을 시간순으로 보여 주고 상세 화면으로 연결한다.
function renderCalendarEvents(events) {
  if (!events.length) {
    return `<div class="calendar-empty"><strong>등록된 일정이 없어요</strong><p>+ 버튼을 눌러 새 일정을 추가해 보세요</p></div>`;
  }
  return events
    .sort((a, b) => a.start.localeCompare(b.start))
    .map((item) => `<button class="calendar-event color-${getEventTone(item.category)}" type="button" data-action="open-event" data-event-id="${item.id}"><span class="event-time">${item.allDay ? "종일" : item.start}</span><span class="event-summary"><strong>${item.title}</strong><small>${item.location || item.category}${item.shared ? " · 공유 일정" : ""}</small></span>${icon("chevron")}</button>`)
    .join("");
}

// 입력값: 없음
// 출력값: 일정 캘린더 화면 HTML 문자열
// 기능: 월간 달력과 선택한 날짜의 일정 목록을 함께 표시한다.
function renderCalendar() {
  const dateKey = toDateKey(state.calendarYear, state.calendarMonth, state.calendarSelectedDay);
  const selectedEvents = state.events.filter((item) => item.date === dateKey);
  return `
    <section class="screen calendar-screen">
      <header class="feature-header"><h1>일정 캘린더</h1><button type="button" data-action="new-event" aria-label="일정 추가">${icon("plus")}</button></header>
      <div class="month-heading"><button type="button" data-action="change-month" data-offset="-1" aria-label="이전 달">${icon("back")}</button><strong>${state.calendarYear}년 ${state.calendarMonth}월</strong><button class="next-month" type="button" data-action="change-month" data-offset="1" aria-label="다음 달">${icon("chevron")}</button></div>
      <div class="calendar-weekdays"><span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span></div>
      <div class="calendar-grid">${renderCalendarGrid()}</div>
      <section class="calendar-agenda"><div class="agenda-heading"><h2>${state.calendarMonth}월 ${state.calendarSelectedDay}일 일정</h2><em>${selectedEvents.length}</em></div><div class="calendar-event-list">${renderCalendarEvents(selectedEvents)}</div></section>
      <button class="floating-button calendar-add" type="button" data-action="new-event" aria-label="일정 추가">${icon("plus")}</button>
      ${renderBottomNav("calendar")}
    </section>`;
}

// 입력값: 아이콘 이름, 항목 제목, 항목 값
// 출력값: 일정 상세 정보 행 HTML 문자열
// 기능: 날짜·장소·메모·알림 정보를 같은 형식으로 표시한다.
function renderEventInfoRow(iconName, label, value) {
  return `<div class="event-info-row"><span class="event-info-icon">${icon(iconName)}</span><div><small>${label}</small><p>${value || "없음"}</p></div></div>`;
}

// 입력값: 없음
// 출력값: 일정 삭제 확인 팝업 HTML 문자열
// 기능: 실수로 일정을 삭제하지 않도록 마지막 확인 단계를 표시한다.
function renderDeleteEventDialog() {
  if (!state.eventDeleteOpen) return "";
  return `<div class="sheet-scrim"></div><section class="confirm-dialog" role="alertdialog" aria-modal="true"><h2>일정을 삭제할까요?</h2><p>삭제한 일정은 다시 복구할 수 없어요.</p><div><button type="button" data-action="cancel-delete-event">취소</button><button class="danger-button" type="button" data-action="confirm-delete-event">삭제</button></div></section>`;
}

// 입력값: 없음
// 출력값: 선택한 일정의 상세 화면 HTML 문자열
// 기능: 일정의 모든 정보를 확인하고 수정·삭제·공유 기능으로 연결한다.
function renderEventDetail() {
  const eventItem = state.events.find((item) => item.id === state.selectedEventId);
  if (!eventItem) {
    state.currentView = "calendar";
    return renderCalendar();
  }
  const timeText = eventItem.allDay ? "종일" : `${eventItem.start} - ${eventItem.end}`;
  return `
    <section class="screen event-detail-screen">
      <header class="feature-header centered"><button type="button" data-action="navigate" data-view="calendar" aria-label="캘린더로 돌아가기">${icon("back")}</button><h1>일정 상세</h1><button type="button" data-action="open-delete-event" aria-label="일정 삭제">${icon("trash")}</button></header>
      <div class="event-detail-scroll">
        <div class="event-title-block"><span class="event-category color-${getEventTone(eventItem.category)}">${eventItem.category}</span><h2>${eventItem.title}</h2>${eventItem.shared ? `<p>그룹과 공유 중인 일정이에요</p>` : ""}</div>
        <div class="event-info-card">
          ${renderEventInfoRow("calendar", "날짜", formatKoreanDate(eventItem.date))}
          ${renderEventInfoRow("clock", "시간", timeText)}
          ${renderEventInfoRow("pin", "장소", eventItem.location)}
          ${renderEventInfoRow("note", "메모", eventItem.memo)}
          ${renderEventInfoRow("bell", "알림", eventItem.alarm)}
        </div>
        <button class="share-event-button" type="button" data-action="open-share-groups">${icon("share")}<span><strong>그룹에 공유</strong><small>내 그룹 구성원과 이 일정을 공유해요</small></span>${icon("chevron")}</button>
      </div>
      <div class="detail-actions"><button type="button" data-action="edit-event">수정하기</button></div>
      ${renderDeleteEventDialog()}
      ${renderShareGroupDialog()}
    </section>`;
}

// 입력값: 없음
// 출력값: 날짜와 시간 선택 바텀시트 HTML 문자열
// 기능: 일정 날짜, 시작 시간, 종료 시간을 한 번에 수정한다.
function renderDateTimePicker() {
  if (!state.dateTimePickerOpen) return "";
  return `<div class="sheet-scrim" data-action="close-date-time-picker"></div><section class="bottom-sheet picker-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>날짜 및 시간</h2><button type="button" data-action="close-date-time-picker">${icon("close")}</button></div><div class="picker-fields"><label class="compact-field"><span>날짜</span><input type="date" data-draft-field="date" value="${state.eventDraft.date}" /></label><div class="field-pair"><label class="compact-field"><span>시작</span><input type="time" data-draft-field="start" value="${state.eventDraft.start}" ${state.eventDraft.allDay ? "disabled" : ""} /></label><label class="compact-field"><span>종료</span><input type="time" data-draft-field="end" value="${state.eventDraft.end}" ${state.eventDraft.allDay ? "disabled" : ""} /></label></div><label class="switch-row"><span><strong>종일</strong><small>시간 없이 날짜만 등록해요</small></span><input type="checkbox" data-draft-field="allDay" ${state.eventDraft.allDay ? "checked" : ""} /></label></div><button class="primary-button" type="button" data-action="close-date-time-picker">확인</button></section>`;
}

// 입력값: 없음
// 출력값: 알림 선택 바텀시트 HTML 문자열
// 기능: 일정 전에 받을 알림 시간을 선택한다.
function renderAlarmPicker() {
  if (!state.alarmPickerOpen) return "";
  const alarms = ["알림 없음", "정시", "10분 전", "30분 전", "1시간 전", "1일 전"];
  return `<div class="sheet-scrim" data-action="close-alarm-picker"></div><section class="bottom-sheet alarm-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>알림</h2><button type="button" data-action="close-alarm-picker">${icon("close")}</button></div><div class="alarm-options">${alarms.map((alarm) => `<button class="${state.eventDraft.alarm === alarm ? "is-selected" : ""}" type="button" data-action="select-alarm" data-alarm="${alarm}"><span>${alarm}</span>${state.eventDraft.alarm === alarm ? icon("check") : ""}</button>`).join("")}</div></section>`;
}

// 입력값: 없음
// 출력값: 일정 추가·수정 화면 HTML 문자열
// 기능: 일정의 제목, 종류, 날짜, 시간, 장소, 메모와 알림을 입력받는다.
function renderEventForm() {
  const isEditing = state.eventFormMode === "edit";
  const timeText = state.eventDraft.allDay ? `${formatKoreanDate(state.eventDraft.date)} · 종일` : `${formatKoreanDate(state.eventDraft.date)} · ${state.eventDraft.start} - ${state.eventDraft.end}`;
  return `
    <section class="screen event-form-screen">
      <header class="feature-header centered"><button type="button" data-action="cancel-event-form" aria-label="이전 화면">${icon("back")}</button><h1>${isEditing ? "일정 수정" : "일정 추가"}</h1><span></span></header>
      <form class="event-form" data-form="event" novalidate>
        <label class="compact-field event-title-field"><span>일정 제목 <b>*</b></span><input name="title" value="${escapeAttribute(state.eventDraft.title)}" placeholder="일정 제목을 입력해 주세요" /></label>
        <fieldset class="category-field"><legend>일정 종류</legend><div>${["개인", "과제", "학교", "그룹"].map((category) => `<button class="${state.eventDraft.category === category ? "is-selected" : ""}" type="button" data-action="select-event-category" data-category="${category}">${category}</button>`).join("")}</div></fieldset>
        <button class="form-navigation-row" type="button" data-action="open-date-time-picker"><span class="event-info-icon">${icon("calendar")}</span><span><small>날짜 및 시간</small><strong>${timeText}</strong></span>${icon("chevron")}</button>
        <label class="compact-field"><span>장소</span><input name="location" value="${escapeAttribute(state.eventDraft.location)}" placeholder="장소를 입력해 주세요" /></label>
        <label class="compact-field"><span>메모</span><textarea name="memo" placeholder="일정에 필요한 내용을 입력해 주세요">${state.eventDraft.memo}</textarea></label>
        <button class="form-navigation-row" type="button" data-action="open-alarm-picker"><span class="event-info-icon">${icon("bell")}</span><span><small>알림</small><strong>${state.eventDraft.alarm}</strong></span>${icon("chevron")}</button>
        <button class="primary-button event-submit" type="submit" ${state.eventDraft.title.trim() ? "" : "disabled"}>${isEditing ? "수정 완료" : "일정 등록"}</button>
      </form>
      ${renderDateTimePicker()}
      ${renderAlarmPicker()}
    </section>`;
}

// 입력값: 없음
// 출력값: 분석 예시 일정 후보 배열
// 기능: 교체 가능한 이미지 분석 단계의 정적 데모 결과를 만든다.
function createExtractionCandidates() {
  return [
    { id: 201, selected: true, title: "영어 수행평가 제출", category: "과제", date: "2026-10-23", start: "23:00", end: "23:59", allDay: false, location: "온라인 클래스", memo: "영어 에세이 최종본 제출", alarm: "1시간 전", confidence: "확인 필요", uncertain: ["종료 시간"] },
    { id: 202, selected: true, title: "수학 수행 보고서", category: "과제", date: "2026-10-23", start: "08:30", end: "09:00", allDay: false, location: "2학년 3반", memo: "인쇄한 보고서 준비", alarm: "1일 전", confidence: "추출 완료", uncertain: [] },
    { id: 203, selected: true, title: "과학 조별 발표", category: "그룹", date: "2026-10-23", start: "14:00", end: "15:00", allDay: false, location: "과학실", memo: "발표 자료와 실험 결과 준비", alarm: "30분 전", confidence: "확인 필요", uncertain: ["장소"] },
  ];
}

// 입력값: 없음
// 출력값: 이미지 파일 선택 영역 HTML 문자열
// 기능: JPEG·PNG 파일을 선택하거나 드래그할 수 있는 첫 업로드 상태를 표시한다.
function renderUploadDropzone() {
  return `<label class="upload-dropzone" for="notice-image"><span class="upload-icon">${icon("upload")}</span><strong>안내문 이미지를 올려 주세요</strong><p>사진 속 날짜와 일정 내용을 찾아드려요</p><em>JPEG, PNG · 최대 10MB</em><input id="notice-image" type="file" accept="image/jpeg,image/png" /></label>`;
}

// 입력값: 없음
// 출력값: 선택한 이미지 미리보기 HTML 문자열
// 기능: 분석 전에 파일명과 원본 이미지를 확인하고 다시 선택할 수 있게 한다.
function renderUploadPreview() {
  return `<div class="upload-preview"><div class="preview-image-wrap"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="선택한 안내문 미리보기" /></div><div class="preview-file-row"><span>${icon("image")}</span><div><strong>${state.upload.fileName}</strong><small>${(state.upload.fileSize / 1024 / 1024).toFixed(1)}MB</small></div><label for="notice-image">다시 선택<input id="notice-image" type="file" accept="image/jpeg,image/png" /></label></div><button class="primary-button" type="button" data-action="analyze-image">일정 분석하기</button></div>`;
}

// 입력값: 제목, 설명, 아이콘 이름
// 출력값: 이미지 분석 오류·빈 결과 상태 HTML 문자열
// 기능: 분석 실패와 일정 미검출 상태에 맞는 안내와 재시도 버튼을 표시한다.
function renderAnalysisMessage(title, description, iconName) {
  return `<div class="analysis-message"><span>${icon(iconName)}</span><h2>${title}</h2><p>${description}</p><button class="primary-button" type="button" data-action="reset-upload">다른 이미지 선택</button>${state.upload.previewUrl ? `<button class="text-retry" type="button" data-action="analyze-image">${icon("refresh")} 다시 분석하기</button>` : ""}</div>`;
}

// 입력값: 없음
// 출력값: 이미지 업로드·미리보기·분석 상태 화면 HTML 문자열
// 기능: 이미지 분석 흐름의 업로드부터 로딩·실패·빈 결과까지 상태별 화면을 제공한다.
function renderUpload() {
  let content = renderUploadDropzone();
  if (state.upload.status === "preview") content = renderUploadPreview();
  if (state.upload.status === "analyzing") content = `<div class="analyzing-state"><div class="scan-preview"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="분석 중인 안내문" /><i></i></div><div class="loading-orbit"></div><h2>이미지에서 일정을 찾고 있어요</h2><p>여러 날짜와 일정 내용을 꼼꼼히 확인하고 있어요</p></div>`;
  if (state.upload.status === "error") content = renderAnalysisMessage("이미지를 분석하지 못했어요", state.upload.error || "이미지가 선명한지 확인하고 다시 시도해 주세요.", "warning");
  if (state.upload.status === "empty") content = renderAnalysisMessage("찾은 일정이 없어요", "날짜와 일정 내용이 잘 보이는 안내문으로 다시 시도해 주세요.", "calendar");

  return `<section class="screen upload-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="home" aria-label="홈으로 돌아가기">${icon("back")}</button><h1>사진으로 일정 추가</h1><span></span></header><div class="upload-content"><div class="upload-copy"><h2>안내문을 일정으로 바꿔드릴게요</h2><p>분석 결과는 등록 전에 직접 확인하고 수정할 수 있어요.</p></div>${content}</div></section>`;
}

// 입력값: 분석된 일정 후보 객체
// 출력값: 선택·수정 가능한 일정 후보 카드 HTML 문자열
// 기능: AI가 추출한 각 일정과 확인이 필요한 필드를 구분해 표시한다.
function renderCandidateCard(candidate) {
  const timeText = candidate.allDay ? "종일" : `${candidate.start} - ${candidate.end}`;
  return `<article class="candidate-card ${candidate.selected ? "is-selected" : ""}"><button class="candidate-check" type="button" data-action="toggle-candidate" data-candidate-id="${candidate.id}" aria-label="${candidate.title} 선택">${candidate.selected ? icon("check") : ""}</button><div class="candidate-main"><div class="candidate-title-row"><span class="event-category color-${getEventTone(candidate.category)}">${candidate.category}</span><em class="${candidate.uncertain.length ? "needs-check" : ""}">${candidate.confidence}</em></div><h3>${candidate.title}</h3><p>${formatKoreanDate(candidate.date)}</p><p>${timeText}${candidate.location ? ` · ${candidate.location}` : ""}</p>${candidate.uncertain.length ? `<small>${icon("warning")} ${candidate.uncertain.join(", ")}을 확인해 주세요</small>` : ""}</div><button class="candidate-edit" type="button" data-action="edit-candidate" data-candidate-id="${candidate.id}" aria-label="${candidate.title} 수정">${icon("pencil")}</button></article>`;
}

// 입력값: 없음
// 출력값: 추출 일정 수정 바텀시트 HTML 문자열
// 기능: 분석 후보의 모호하거나 잘못된 정보를 등록 전에 직접 수정한다.
function renderCandidateEditor() {
  if (!state.candidateDraft) return "";
  return `<div class="sheet-scrim" data-action="close-candidate-editor"></div><section class="bottom-sheet candidate-editor-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>추출 일정 수정</h2><button type="button" data-action="close-candidate-editor">${icon("close")}</button></div><form class="candidate-form" data-form="candidate"><label class="compact-field"><span>일정 제목</span><input name="title" value="${escapeAttribute(state.candidateDraft.title)}" /></label><div class="field-pair"><label class="compact-field"><span>날짜</span><input type="date" name="date" value="${state.candidateDraft.date}" /></label><label class="compact-field"><span>종류</span><select name="category">${["개인", "과제", "학교", "그룹"].map((category) => `<option ${category === state.candidateDraft.category ? "selected" : ""}>${category}</option>`).join("")}</select></label></div><div class="field-pair"><label class="compact-field"><span>시작</span><input type="time" name="start" value="${state.candidateDraft.start}" /></label><label class="compact-field"><span>종료</span><input type="time" name="end" value="${state.candidateDraft.end}" /></label></div><label class="compact-field"><span>장소</span><input name="location" value="${escapeAttribute(state.candidateDraft.location)}" /></label><label class="compact-field"><span>메모</span><textarea name="memo">${state.candidateDraft.memo}</textarea></label><button class="primary-button" type="submit">수정 완료</button></form></section>`;
}

// 입력값: 없음
// 출력값: 일정 등록 완료 팝업 HTML 문자열
// 기능: 선택한 분석 후보가 캘린더에 등록됐음을 알리고 캘린더로 이동시킨다.
function renderRegistrationComplete() {
  if (!state.registrationComplete) return "";
  return `<div class="sheet-scrim"></div><section class="complete-dialog" role="dialog" aria-modal="true"><span>${icon("check")}</span><h2>일정 등록이 완료됐어요</h2><p>캘린더에서 등록한 일정을 확인할 수 있어요.</p><button class="primary-button" type="button" data-action="finish-registration">캘린더 확인</button></section>`;
}

// 입력값: 없음
// 출력값: 이미지 분석 결과 확인 화면 HTML 문자열
// 기능: 여러 일정 후보를 선택·수정한 뒤 한 번에 캘린더에 등록한다.
function renderExtractionResults() {
  const selectedCount = state.extractionCandidates.filter((candidate) => candidate.selected).length;
  return `<section class="screen extraction-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="upload" aria-label="업로드로 돌아가기">${icon("back")}</button><h1>분석 결과 확인</h1><span></span></header><div class="result-scroll"><div class="result-summary"><div><strong>${state.extractionCandidates.length}개의 일정을 찾았어요</strong><p>잘못 읽은 내용이 없는지 확인해 주세요.</p></div><span>부분 추출</span></div><div class="source-preview"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="원본 안내문" /><div><strong>원본 이미지</strong><small>${state.upload.fileName}</small></div></div><div class="candidate-list">${state.extractionCandidates.map(renderCandidateCard).join("")}</div></div><div class="result-actions"><p><strong>${selectedCount}</strong>개 일정 선택</p><button class="primary-button" type="button" data-action="register-candidates" ${selectedCount ? "" : "disabled"}>선택 일정 등록</button></div>${renderCandidateEditor()}${renderRegistrationComplete()}</section>`;
}

// 입력값: 아이콘 이름, 제목, 보조 설명, 실행 이름
// 출력값: 마이 화면의 메뉴 행 HTML 문자열
// 기능: 계정 관련 메뉴를 일관된 카드 형태로 표시한다.
function renderMyMenuRow(iconName, title, description, action) {
  return `<button class="my-menu-row" type="button" data-action="${action}"><span>${icon(iconName)}</span><span><strong>${title}</strong><small>${description}</small></span>${icon("chevron")}</button>`;
}

// 입력값: 없음
// 출력값: 마이 화면 HTML 문자열
// 기능: 사용자 정보, 그룹 관리, 알림 설정과 로그아웃 기능을 표시한다.
function renderMy() {
  const userName = state.signup.name.trim() || "김플래닛";
  const userEmail = state.signup.email.trim() || state.login.email.trim() || "student@planit.kr";
  return `<section class="screen my-screen"><header class="feature-header"><h1>마이</h1><span></span></header><div class="my-scroll"><section class="profile-card"><span class="profile-avatar">${userName.slice(0, 1)}</span><div><h2>${userName}</h2><p>${userEmail}</p><small>고등학생 · 2학년</small></div></section><section class="my-menu-card">${renderMyMenuRow("users", "내 그룹", `${state.groups.length}개 그룹에 참여 중`, "open-groups")}<label class="notification-row"><span class="my-menu-icon">${icon("bell")}</span><span><strong>일정 알림</strong><small>마감과 시작 전에 알려드려요</small></span><input type="checkbox" data-action="toggle-notification" ${state.notificationEnabled ? "checked" : ""} /></label>${renderMyMenuRow("note", "이용약관 및 개인정보", "PLANIT 서비스 정책 확인", "open-terms-info")}</section><button class="logout-button" type="button" data-action="logout">${icon("logout")} 로그아웃</button><p class="app-version">PLANIT v1.0.0</p></div>${renderBottomNav("my")}</section>`;
}

// 입력값: 없음
// 출력값: 서비스 약관 안내 화면 HTML 문자열
// 기능: 가입 시 동의한 서비스 이용 및 개인정보 안내를 다시 확인하게 한다.
function renderTermsInfo() {
  return `<section class="screen terms-info-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="my" aria-label="마이로 돌아가기">${icon("back")}</button><h1>이용약관 및 개인정보</h1><span></span></header><div class="terms-info-scroll"><section><h2>서비스 이용약관</h2><p>PLANIT은 안내문에서 추출한 일정 정보를 사용자가 확인하고 관리할 수 있도록 돕는 서비스입니다. 분석 결과는 자동 확정되지 않으며 등록 전에 사용자가 확인해야 합니다.</p></section><section><h2>개인정보 수집·이용 안내</h2><p>계정과 일정 제공에 필요한 최소 정보만 사용합니다. 이미지 원문과 민감 정보는 불필요하게 노출하거나 기록하지 않습니다.</p></section><section><h2>일정 알림 안내</h2><p>선택한 알림 시간에 따라 일정 시작 또는 마감 전에 안내합니다. 마이 화면에서 언제든 알림을 끌 수 있습니다.</p></section></div></section>`;
}

// 입력값: 그룹 객체
// 출력값: 그룹 목록 카드 HTML 문자열
// 기능: 참여 중인 그룹의 이름, 설명, 인원과 공유 일정 수를 보여 준다.
function renderGroupCard(group) {
  return `<button class="group-card" type="button" data-action="open-group" data-group-id="${group.id}"><span class="group-symbol color-${group.color}">${group.name.slice(0, 1)}</span><span class="group-card-main"><strong>${group.name}</strong><small>${group.description}</small><em>${icon("users")} ${group.members.length}명 · 공유 일정 ${group.eventIds.length}개</em></span>${icon("chevron")}</button>`;
}

// 입력값: 없음
// 출력값: 그룹 생성·참여 바텀시트 HTML 문자열
// 기능: 새 그룹 이름을 등록하거나 초대 코드로 기존 그룹에 참여한다.
function renderGroupModal() {
  if (!state.groupModalOpen) return "";
  const isCreate = state.groupModalTab === "create";
  return `<div class="sheet-scrim" data-action="close-group-modal"></div><section class="bottom-sheet group-modal-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>그룹 추가</h2><button type="button" data-action="close-group-modal">${icon("close")}</button></div><div class="segment-control group-segments"><button class="${isCreate ? "is-active" : ""}" type="button" data-action="set-group-tab" data-tab="create">그룹 만들기</button><button class="${!isCreate ? "is-active" : ""}" type="button" data-action="set-group-tab" data-tab="join">코드로 참여</button></div><form class="group-form" data-form="group"><div class="group-modal-icon">${icon(isCreate ? "users" : "link")}</div><h3>${isCreate ? "새 그룹을 만들어 보세요" : "초대 코드를 입력해 주세요"}</h3><p>${isCreate ? "반, 동아리, 스터디 일정을 함께 관리할 수 있어요." : "그룹 관리자에게 받은 코드를 입력하면 바로 참여해요."}</p>${isCreate ? `<label class="compact-field"><span>그룹 이름</span><input name="name" value="${escapeAttribute(state.groupDraft.name)}" placeholder="예: 2학년 3반" /></label>` : `<label class="compact-field"><span>초대 코드</span><input name="code" value="${escapeAttribute(state.groupDraft.code)}" placeholder="PLN-XXXX" /></label>`}${state.groupError ? `<small class="group-error">${state.groupError}</small>` : ""}<button class="primary-button" type="submit" ${(isCreate ? state.groupDraft.name.trim() : state.groupDraft.code.trim()) ? "" : "disabled"}>${isCreate ? "그룹 만들기" : "그룹 참여"}</button></form></section>`;
}

// 입력값: 없음
// 출력값: 그룹 목록 화면 HTML 문자열
// 기능: 참여 그룹을 확인하고 그룹 생성 또는 코드 참여를 시작한다.
function renderGroups() {
  return `<section class="screen groups-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="my" aria-label="마이로 돌아가기">${icon("back")}</button><h1>내 그룹</h1><button type="button" data-action="open-group-modal" aria-label="그룹 추가">${icon("plus")}</button></header><div class="groups-scroll"><div class="groups-heading"><h2>함께하는 그룹</h2><p>그룹 일정을 내 캘린더에서 한눈에 확인해요.</p></div><div class="group-list">${state.groups.map(renderGroupCard).join("")}</div></div>${renderGroupModal()}</section>`;
}

// 입력값: 그룹에서 공유 중인 일정 ID 목록
// 출력값: 공유 일정 카드 HTML 문자열
// 기능: 그룹 상세 화면에서 구성원이 함께 보는 일정을 보여 준다.
function renderGroupEvents(eventIds) {
  const events = state.events.filter((item) => eventIds.includes(item.id));
  if (!events.length) return `<p class="group-empty">아직 공유된 일정이 없어요.</p>`;
  return events.map((item) => `<button class="group-event-row" type="button" data-action="open-event" data-event-id="${item.id}"><span>${item.date.slice(5).replace("-", "/")}</span><div><strong>${item.title}</strong><small>${item.start} · ${item.location || "장소 없음"}</small></div>${icon("chevron")}</button>`).join("");
}

// 입력값: 없음
// 출력값: 선택한 그룹 상세 화면 HTML 문자열
// 기능: 그룹 구성원, 초대 코드와 공유 일정을 확인하고 새 그룹 일정을 등록한다.
function renderGroupDetail() {
  const group = state.groups.find((item) => item.id === state.selectedGroupId) ?? state.groups[0];
  return `<section class="screen group-detail-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="groups" aria-label="그룹 목록으로 돌아가기">${icon("back")}</button><h1>그룹 상세</h1><span></span></header><div class="group-detail-scroll"><section class="group-hero color-${group.color}"><span>${group.name.slice(0, 1)}</span><div><h2>${group.name}</h2><p>${group.description}</p></div></section><section class="invite-code-card"><div><small>초대 코드</small><strong>${group.code}</strong></div><button type="button" data-action="copy-group-code" data-code="${group.code}">${icon("link")} 복사</button></section><section class="group-section"><div class="group-section-heading"><h3>구성원</h3><span>${group.members.length}명</span></div><div class="member-stack">${group.members.slice(0, 5).map((member, index) => `<span title="${member}" style="--member-index:${index}">${member.slice(0, 1)}</span>`).join("")}</div><p class="member-names">${group.members.join(" · ")}</p></section><section class="group-section"><div class="group-section-heading"><h3>공유 일정</h3><button type="button" data-action="new-group-event">+ 일정 추가</button></div><div class="group-event-list">${renderGroupEvents(group.eventIds)}</div></section></div></section>`;
}

// 입력값: 없음
// 출력값: 그룹 공유 선택 팝업 HTML 문자열
// 기능: 현재 일정을 공유할 그룹을 복수 선택하고 적용한다.
function renderShareGroupDialog() {
  if (!state.groupShareOpen) return "";
  return `<div class="sheet-scrim" data-action="close-share-groups"></div><section class="bottom-sheet share-groups-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>그룹에 공유</h2><button type="button" data-action="close-share-groups">${icon("close")}</button></div><p class="share-help">이 일정을 함께 볼 그룹을 선택해 주세요.</p><div class="share-group-list">${state.groups.map((group) => { const selected = state.selectedShareGroupIds.includes(group.id); return `<button class="${selected ? "is-selected" : ""}" type="button" data-action="toggle-share-group" data-group-id="${group.id}"><span class="group-symbol color-${group.color}">${group.name.slice(0, 1)}</span><span><strong>${group.name}</strong><small>${group.members.length}명</small></span><i>${selected ? icon("check") : ""}</i></button>`; }).join("")}</div><button class="primary-button" type="button" data-action="confirm-share-groups" ${state.selectedShareGroupIds.length ? "" : "disabled"}>선택 그룹에 공유</button></section>`;
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
    calendar: renderCalendar,
    eventDetail: renderEventDetail,
    eventForm: renderEventForm,
    upload: renderUpload,
    extractionResults: renderExtractionResults,
    my: renderMy,
    termsInfo: renderTermsInfo,
    groups: renderGroups,
    groupDetail: renderGroupDetail,
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

  if (input.dataset.draftField) {
    state.eventDraft[input.dataset.draftField] = input.type === "checkbox" ? input.checked : input.value;
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

  if (form.dataset.form === "event") {
    state.eventDraft[input.name] = input.value;
    const submitButton = form.querySelector('[type="submit"]');
    if (submitButton) submitButton.disabled = !state.eventDraft.title.trim();
  }
  if (form.dataset.form === "candidate" && state.candidateDraft) {
    state.candidateDraft[input.name] = input.value;
  }
  if (form.dataset.form === "group") {
    state.groupDraft[input.name] = input.value;
    state.groupError = "";
    const submitButton = form.querySelector('[type="submit"]');
    if (submitButton) submitButton.disabled = !input.value.trim();
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


  if (form.dataset.form === "event" && state.eventDraft.title.trim()) {
    if (!state.eventDraft.allDay && state.eventDraft.end <= state.eventDraft.start) {
      state.eventDraft.end = state.eventDraft.start;
      state.alarmPickerOpen = false;
      renderApp();
      return;
    }
    if (state.eventFormMode === "edit") {
      const eventItem = state.events.find((item) => item.id === state.selectedEventId);
      Object.assign(eventItem, state.eventDraft);
    } else {
      const isGroupEvent = state.eventReturnView === "groupDetail";
      const newEvent = { id: Date.now(), ...state.eventDraft, shared: isGroupEvent };
      state.events.push(newEvent);
      state.selectedEventId = newEvent.id;
      if (isGroupEvent) {
        const group = state.groups.find((item) => item.id === state.selectedGroupId);
        if (group) group.eventIds.push(newEvent.id);
      }
    }
    const [year, month, day] = state.eventDraft.date.split("-").map(Number);
    state.calendarYear = year;
    state.calendarMonth = month;
    state.calendarSelectedDay = day;
    state.currentView = "eventDetail";
    renderApp();
  }


  if (form.dataset.form === "candidate" && state.candidateDraft?.title.trim()) {
    const candidate = state.extractionCandidates.find((item) => item.id === state.editingCandidateId);
    Object.assign(candidate, state.candidateDraft, { uncertain: [], confidence: "수정 완료" });
    state.candidateDraft = null;
    state.editingCandidateId = null;
    renderApp();
  }


  if (form.dataset.form === "group") {
    if (state.groupModalTab === "create" && state.groupDraft.name.trim()) {
      const group = { id: Date.now(), name: state.groupDraft.name.trim(), description: "새로운 일정 공유 그룹", code: `PLN-${String(Date.now()).slice(-6)}`, color: "violet", members: [state.signup.name.trim() || "김플래닛"], eventIds: [] };
      state.groups.push(group);
      state.selectedGroupId = group.id;
      state.groupModalOpen = false;
      state.groupDraft = { name: "", code: "" };
      state.currentView = "groupDetail";
      renderApp();
    }
    if (state.groupModalTab === "join" && state.groupDraft.code.trim()) {
      const group = state.groups.find((item) => item.code.toLowerCase() === state.groupDraft.code.trim().toLowerCase());
      if (!group) {
        state.groupError = "초대 코드를 다시 확인해 주세요.";
      } else {
        state.selectedGroupId = group.id;
        state.groupModalOpen = false;
        state.groupDraft = { name: "", code: "" };
        state.currentView = "groupDetail";
      }
      renderApp();
    }
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

  if (action === "change-month") {
    const nextDate = new Date(state.calendarYear, state.calendarMonth - 1 + Number(target.dataset.offset), 1);
    state.calendarYear = nextDate.getFullYear();
    state.calendarMonth = nextDate.getMonth() + 1;
    state.calendarSelectedDay = 1;
  }
  if (action === "select-calendar-date") {
    const [year, month, day] = target.dataset.date.split("-").map(Number);
    state.calendarYear = year;
    state.calendarMonth = month;
    state.calendarSelectedDay = day;
  }
  if (action === "open-event") {
    state.selectedEventId = Number(target.dataset.eventId);
    state.currentView = "eventDetail";
  }
  if (action === "new-event") {
    state.addMenuOpen = false;
    openEventForm(null, state.currentView === "home" ? "home" : "calendar");
  }
  if (action === "edit-event") {
    const eventItem = state.events.find((item) => item.id === state.selectedEventId);
    if (eventItem) openEventForm(eventItem, "eventDetail");
  }
  if (action === "cancel-event-form") state.currentView = state.eventReturnView;
  if (action === "open-delete-event") state.eventDeleteOpen = true;
  if (action === "cancel-delete-event") state.eventDeleteOpen = false;
  if (action === "confirm-delete-event") {
    state.events = state.events.filter((item) => item.id !== state.selectedEventId);
    state.selectedEventId = null;
    state.eventDeleteOpen = false;
    state.currentView = "calendar";
  }
  if (action === "select-event-category") state.eventDraft.category = target.dataset.category;
  if (action === "open-date-time-picker") state.dateTimePickerOpen = true;
  if (action === "close-date-time-picker") state.dateTimePickerOpen = false;
  if (action === "open-alarm-picker") state.alarmPickerOpen = true;
  if (action === "close-alarm-picker") state.alarmPickerOpen = false;
  if (action === "select-alarm") {
    state.eventDraft.alarm = target.dataset.alarm;
    state.alarmPickerOpen = false;
  }
  if (action === "reset-upload") {
    if (state.upload.previewUrl) URL.revokeObjectURL(state.upload.previewUrl);
    state.upload = { status: "idle", fileName: "", fileSize: 0, previewUrl: "", error: "" };
    state.extractionCandidates = [];
  }
  if (action === "analyze-image" && state.upload.previewUrl) {
    state.upload.status = "analyzing";
    renderApp();
    window.setTimeout(() => {
      if (state.upload.fileSize === 0) {
        state.upload.status = "empty";
        renderApp();
        return;
      }
      state.extractionCandidates = createExtractionCandidates();
      state.currentView = "extractionResults";
      renderApp();
    }, 850);
    return;
  }
  if (action === "toggle-candidate") {
    const candidate = state.extractionCandidates.find((item) => item.id === Number(target.dataset.candidateId));
    if (candidate) candidate.selected = !candidate.selected;
  }
  if (action === "edit-candidate") {
    const candidate = state.extractionCandidates.find((item) => item.id === Number(target.dataset.candidateId));
    if (candidate) {
      state.editingCandidateId = candidate.id;
      state.candidateDraft = { ...candidate };
    }
  }
  if (action === "close-candidate-editor") {
    state.editingCandidateId = null;
    state.candidateDraft = null;
  }
  if (action === "register-candidates") {
    const selectedCandidates = state.extractionCandidates.filter((candidate) => candidate.selected);
    selectedCandidates.forEach((candidate, index) => {
      state.events.push({ ...candidate, id: Date.now() + index, shared: false });
    });
    state.registrationComplete = true;
  }
  if (action === "finish-registration") {
    state.registrationComplete = false;
    state.currentView = "calendar";
  }
  if (action === "open-groups") state.currentView = "groups";
  if (action === "open-terms-info") state.currentView = "termsInfo";
  if (action === "toggle-notification") state.notificationEnabled = !state.notificationEnabled;
  if (action === "logout") {
    state.currentView = "login";
    state.login.password = "";
  }
  if (action === "open-group-modal") {
    state.groupModalOpen = true;
    state.groupModalTab = "create";
    state.groupError = "";
  }
  if (action === "close-group-modal") state.groupModalOpen = false;
  if (action === "set-group-tab") {
    state.groupModalTab = target.dataset.tab;
    state.groupError = "";
  }
  if (action === "open-group") {
    state.selectedGroupId = Number(target.dataset.groupId);
    state.currentView = "groupDetail";
  }
  if (action === "copy-group-code") {
    navigator.clipboard?.writeText(target.dataset.code).catch(() => {});
    target.textContent = "복사됨";
    return;
  }
  if (action === "new-group-event") {
    openEventForm(null, "groupDetail");
    state.eventDraft.category = "그룹";
  }
  if (action === "open-share-groups") {
    state.groupShareOpen = true;
    state.selectedShareGroupIds = state.groups.filter((group) => group.eventIds.includes(state.selectedEventId)).map((group) => group.id);
  }
  if (action === "close-share-groups") state.groupShareOpen = false;
  if (action === "toggle-share-group") {
    const groupId = Number(target.dataset.groupId);
    state.selectedShareGroupIds = state.selectedShareGroupIds.includes(groupId) ? state.selectedShareGroupIds.filter((id) => id !== groupId) : [...state.selectedShareGroupIds, groupId];
  }
  if (action === "confirm-share-groups") {
    state.groups.forEach((group) => {
      const shouldShare = state.selectedShareGroupIds.includes(group.id);
      const hasEvent = group.eventIds.includes(state.selectedEventId);
      if (shouldShare && !hasEvent) group.eventIds.push(state.selectedEventId);
      if (!shouldShare && hasEvent) group.eventIds = group.eventIds.filter((id) => id !== state.selectedEventId);
    });
    const eventItem = state.events.find((item) => item.id === state.selectedEventId);
    if (eventItem) eventItem.shared = state.selectedShareGroupIds.length > 0;
    state.groupShareOpen = false;
  }

  renderApp();
}

// 입력값: 파일 선택 변경 이벤트
// 출력값: 없음
// 기능: 이미지 형식과 용량을 검증하고 안전한 로컬 미리보기 주소를 만든다.
function handleFileChange(event) {
  const input = event.target.closest('input[type="file"]');
  if (!input) return;
  const [file] = input.files;
  if (!file) return;
  const allowedTypes = ["image/jpeg", "image/png"];
  if (!allowedTypes.includes(file.type)) {
    state.upload = { status: "error", fileName: file.name, fileSize: file.size, previewUrl: "", error: "JPEG 또는 PNG 형식의 이미지만 올릴 수 있어요." };
    renderApp();
    return;
  }
  if (file.size > 10 * 1024 * 1024) {
    state.upload = { status: "error", fileName: file.name, fileSize: file.size, previewUrl: "", error: "이미지 용량은 10MB 이하로 올려 주세요." };
    renderApp();
    return;
  }
  if (state.upload.previewUrl) URL.revokeObjectURL(state.upload.previewUrl);
  state.upload = { status: "preview", fileName: file.name, fileSize: file.size, previewUrl: URL.createObjectURL(file), error: "" };
  renderApp();
}

app.addEventListener("input", handleInput);
app.addEventListener("change", handleFileChange);
app.addEventListener("submit", handleSubmit);
app.addEventListener("click", handleClick);
renderApp();
