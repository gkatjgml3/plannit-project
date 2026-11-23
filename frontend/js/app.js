const app = document.querySelector("#app");
const temporaryStorageKey = "planitTemporaryData";
const deviceToday = new Date();
const deviceTodayKey = toDateKey(deviceToday.getFullYear(), deviceToday.getMonth() + 1, deviceToday.getDate());
const defaultAfterSchoolPeriod = Object.freeze({ period: 8, label: "방과후", start: "16:10", end: "17:00", isAfterSchool: true });

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
    school: "",
    grade: "2학년",
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
  termsDetail: null,
  selectedDate: deviceTodayKey,
  homeFilter: "all",
  addMenuOpen: false,
  calendarFilter: "all",
  notificationFilter: "all",
  notificationEnabled: true,
  groupNotificationEnabled: true,
  deadlineNotificationEnabled: true,
  searchOpen: false,
  cameraPermissionSeen: false,
  selectedWeekday: "월",
  timetableSettingsTab: "subjects",
  classEditorOpen: false,
  editingClassId: null,
  classColorChanged: false,
  classDraft: {
    day: "월",
    period: "1",
    title: "",
    teacher: "",
    room: "",
    color: "violet",
  },
  timetable: [
    { id: 1, day: "월", period: 1, title: "학급자치", teacher: "", room: "", color: "violet" },
    { id: 2, day: "화", period: 1, title: "일본어", teacher: "", room: "", color: "blue" },
    { id: 3, day: "수", period: 1, title: "JAVA", teacher: "", room: "", color: "green" },
    { id: 4, day: "목", period: 1, title: "공통영어", teacher: "", room: "", color: "orange" },
    { id: 5, day: "금", period: 1, title: "공통국어(이)", teacher: "", room: "", color: "pink" },
    { id: 6, day: "월", period: 2, title: "한국사", teacher: "", room: "", color: "yellow" },
    { id: 7, day: "화", period: 2, title: "공통영어", teacher: "", room: "", color: "orange" },
    { id: 8, day: "수", period: 2, title: "JAVA", teacher: "", room: "", color: "green" },
    { id: 9, day: "목", period: 2, title: "JAVA", teacher: "", room: "", color: "green" },
    { id: 10, day: "금", period: 2, title: "공통영어", teacher: "", room: "", color: "orange" },
    { id: 11, day: "월", period: 3, title: "일본어", teacher: "", room: "", color: "blue" },
    { id: 12, day: "화", period: 3, title: "한국사", teacher: "", room: "", color: "yellow" },
    { id: 13, day: "수", period: 3, title: "일본어", teacher: "", room: "", color: "blue" },
    { id: 14, day: "목", period: 3, title: "MC", teacher: "", room: "", color: "violet" },
    { id: 15, day: "금", period: 3, title: "기술가정", teacher: "", room: "", color: "pink" },
    { id: 16, day: "월", period: 4, title: "MC", teacher: "", room: "", color: "violet" },
    { id: 17, day: "화", period: 4, title: "공통국어(손)", teacher: "", room: "", color: "pink" },
    { id: 18, day: "수", period: 4, title: "공통수학", teacher: "", room: "", color: "blue" },
    { id: 19, day: "목", period: 4, title: "MC", teacher: "", room: "", color: "violet" },
    { id: 20, day: "금", period: 4, title: "공통수학", teacher: "", room: "", color: "blue" },
    { id: 21, day: "월", period: 5, title: "JAVA", teacher: "", room: "", color: "green" },
    { id: 22, day: "화", period: 5, title: "공통국어(이)", teacher: "", room: "", color: "pink" },
    { id: 23, day: "수", period: 5, title: "성공적인 직업생활", teacher: "", room: "", color: "yellow" },
    { id: 24, day: "목", period: 5, title: "공통수학", teacher: "", room: "", color: "blue" },
    { id: 25, day: "금", period: 5, title: "성공적인 직업생활", teacher: "", room: "", color: "yellow" },
    { id: 26, day: "월", period: 6, title: "JAVA", teacher: "", room: "", color: "green" },
    { id: 27, day: "화", period: 6, title: "체육", teacher: "", room: "", color: "orange" },
    { id: 28, day: "수", period: 6, title: "동아리 멘토링", teacher: "", room: "", color: "violet" },
    { id: 29, day: "목", period: 6, title: "체육", teacher: "", room: "", color: "orange" },
    { id: 30, day: "금", period: 6, title: "한국사", teacher: "", room: "", color: "yellow" },
    { id: 31, day: "화", period: 7, title: "기술가정", teacher: "", room: "", color: "pink" },
    { id: 32, day: "수", period: 7, title: "동아리 멘토링", teacher: "", room: "", color: "violet" },
    { id: 33, day: "목", period: 7, title: "기술가정", teacher: "", room: "", color: "pink" },
  ],
  periodTimes: [
    { period: 1, start: "08:20", end: "09:10" },
    { period: 2, start: "09:20", end: "10:10" },
    { period: 3, start: "10:20", end: "11:10" },
    { period: 4, start: "11:20", end: "12:10" },
    { period: 5, start: "13:00", end: "13:50" },
    { period: 6, start: "14:00", end: "14:50" },
    { period: 7, start: "15:00", end: "15:50" },
    { ...defaultAfterSchoolPeriod },
  ],
  lunchTime: { start: "12:10", end: "13:00" },
  routineTimes: {
    morning: { label: "조회", start: "08:05", end: "08:20", note: "" },
    cleaning: { label: "청소", start: "15:50", end: "16:00", note: "14:50~15:00" },
    homeroom: { label: "종례", start: "16:00", end: "", note: "15:00~" },
  },
  timetableTimeDraft: null,
  timetableTimeError: "",
  calendarYear: deviceToday.getFullYear(),
  calendarMonth: deviceToday.getMonth() + 1,
  calendarSelectedDay: deviceToday.getDate(),
  selectedEventId: null,
  eventFormMode: "create",
  eventReturnView: "calendar",
  eventDetailReturnView: "calendar",
  eventFormError: "",
  eventDeleteOpen: false,
  dateTimePickerOpen: false,
  alarmPickerOpen: false,
  eventDraft: {
    title: "",
    category: "개인",
    date: deviceTodayKey,
    start: "09:00",
    end: "10:00",
    allDay: false,
    location: "",
    preparation: "",
    memo: "",
    alarm: "10분 전",
    repeat: "반복 안 함",
    sharedGroup: "공유 안 함",
  },
  events: [
    { id: 101, title: "JAVA 수행평가", category: "개인", date: "2026-11-17", start: "09:00", end: "10:00", allDay: false, location: "컴퓨터실", preparation: "노트북, 수행평가 자료", memo: "10분 전에 도착하기", alarm: "10분 전", repeat: "반복 안 함", sharedGroup: "공유 안 함", shared: false },
    { id: 102, title: "동아리 멘토링", category: "그룹", date: "2026-11-17", start: "14:00", end: "15:00", allDay: false, location: "3층 세미나실", preparation: "멘토링 활동지", memo: "프로젝트 진행 상황 공유", alarm: "10분 전", repeat: "매주 화요일", sharedGroup: "코딩 동아리", shared: true },
    { id: 103, title: "과학 보고서 제출", category: "개인", date: "2026-11-17", start: "16:00", end: "17:00", allDay: false, location: "온라인 제출", preparation: "최종 보고서 PDF", memo: "파일명 확인", alarm: "1시간 전", repeat: "반복 안 함", sharedGroup: "공유 안 함", shared: false },
    { id: 104, title: "학급 사진 촬영", category: "그룹", date: "2026-11-18", start: "11:20", end: "12:10", allDay: false, location: "운동장", preparation: "교복", memo: "우천 시 체육관", alarm: "30분 전", repeat: "반복 안 함", sharedGroup: "2학년 3반", shared: true },
    { id: 105, title: "프로젝트 중간 점검", category: "그룹", date: "2026-11-22", start: "14:00", end: "15:00", allDay: false, location: "동아리실", preparation: "발표 자료", memo: "진행률 점검", alarm: "1일 전", repeat: "반복 안 함", sharedGroup: "코딩 동아리", shared: true },
    { id: 106, title: "발표 자료 합치기", category: "그룹", date: "2026-11-26", start: "18:00", end: "19:00", allDay: false, location: "온라인", preparation: "개인 발표 자료", memo: "공유 문서에서 작업", alarm: "1시간 전", repeat: "반복 안 함", sharedGroup: "코딩 동아리", shared: true },
    { id: 107, title: "최종 발표", category: "그룹", date: "2026-12-03", start: "15:00", end: "16:00", allDay: false, location: "대강당", preparation: "발표 자료, 노트북", memo: "리허설 30분 전", alarm: "1일 전", repeat: "반복 안 함", sharedGroup: "코딩 동아리", shared: true },
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
  groupDetailTab: "events",
  groupDraft: { name: "", code: "", description: "", type: "동아리", icon: "users" },
  groupError: "",
  selectedGroupId: 1,
  groupShareOpen: false,
  selectedShareGroupIds: [],
  groups: [
    { id: 1, name: "2학년 3반", description: "학급 일정과 공지를 함께 관리해요", code: "PL-23CLASS", color: "pink", members: ["지민", "민서", "수현", "현우", "서윤", "도윤"], eventIds: [104] },
    { id: 2, name: "코딩 동아리", description: "함께 코딩하고 발표 일정을 공유해요", code: "PL-7K2M-90", color: "pink", members: ["지민", "민서", "수현", "현우", "유진", "서준", "하윤", "도윤", "서윤", "민준", "예은", "준호"], eventIds: [102, 105, 106, 107] },
    { id: 3, name: "수학 스터디", description: "개념 문제 풀이와 시험 일정을 나눠요", code: "PL-MATH-05", color: "yellow", members: ["지민", "민서", "수현", "현우", "유진"], eventIds: [] },
    { id: 4, name: "조별 과제 A팀", description: "수행평가 자료와 마감 일정을 공유해요", code: "PL-TEAM-A4", color: "blue", members: ["지민", "민서", "수현", "현우"], eventIds: [103] },
  ],
};

// 입력값: 없음
// 출력값: 없음
// 기능: DB 연결 전 사용자가 등록한 일정과 시간표 변경값을 현재 브라우저 탭에 임시 저장한다.
function persistTemporaryData() {
  const temporaryData = {
    events: state.events,
    timetable: state.timetable,
    periodTimes: state.periodTimes,
    lunchTime: state.lunchTime,
    routineTimes: state.routineTimes,
    groups: state.groups,
  };

  try {
    window.sessionStorage.setItem(temporaryStorageKey, JSON.stringify(temporaryData));
  } catch (error) {
    console.warn("임시 데이터를 저장하지 못했습니다.", error);
  }
}

// 입력값: 없음
// 출력값: 없음
// 기능: 같은 브라우저 탭에서 새로고침해도 임시 등록 내용을 이어서 볼 수 있게 복원한다.
function restoreTemporaryData() {
  try {
    const savedData = JSON.parse(window.sessionStorage.getItem(temporaryStorageKey) || "null");
    if (!savedData || typeof savedData !== "object") return;
    if (Array.isArray(savedData.events)) state.events = savedData.events;
    if (Array.isArray(savedData.timetable)) state.timetable = savedData.timetable;
    if (Array.isArray(savedData.periodTimes)) {
      state.periodTimes = savedData.periodTimes.map((period) => period.period === defaultAfterSchoolPeriod.period
        ? { ...defaultAfterSchoolPeriod, ...period, label: period.label || defaultAfterSchoolPeriod.label, isAfterSchool: true }
        : period);
      if (!state.periodTimes.some((period) => period.period === defaultAfterSchoolPeriod.period)) {
        state.periodTimes.push({ ...defaultAfterSchoolPeriod });
      }
    }
    if (savedData.lunchTime && typeof savedData.lunchTime === "object") state.lunchTime = savedData.lunchTime;
    if (savedData.routineTimes && typeof savedData.routineTimes === "object") state.routineTimes = savedData.routineTimes;
    if (Array.isArray(savedData.groups)) state.groups = savedData.groups;
  } catch (error) {
    console.warn("임시 데이터를 불러오지 못했습니다.", error);
  }
}

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
  search: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="10.5" cy="10.5" r="6.5" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="m15.5 15.5 4 4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  more: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="5" cy="12" r="1.6" fill="currentColor"/><circle cx="12" cy="12" r="1.6" fill="currentColor"/><circle cx="19" cy="12" r="1.6" fill="currentColor"/></svg>`,
  camera: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M8 6.5 9.4 4h5.2L16 6.5h2.5A2.5 2.5 0 0 1 21 9v8.5a2.5 2.5 0 0 1-2.5 2.5h-13A2.5 2.5 0 0 1 3 17.5V9a2.5 2.5 0 0 1 2.5-2.5H8Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><circle cx="12" cy="13" r="4" fill="none" stroke="currentColor" stroke-width="1.8"/></svg>`,
  flash: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m13.5 2-7 11h5l-1 9 7-12h-5l1-8Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  sun: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="3.5" fill="none" stroke="currentColor" stroke-width="1.7"/><path d="M12 2v3M12 19v3M2 12h3M19 12h3M4.9 4.9 7 7M17 17l2.1 2.1M19.1 4.9 17 7M7 17l-2.1 2.1" fill="none" stroke="currentColor" stroke-width="1.7" stroke-linecap="round"/></svg>`,
  lock: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="10" width="14" height="10" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M8 10V7a4 4 0 0 1 8 0v3" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
};

// 입력값: 아이콘 이름
// 출력값: 화면에 삽입할 SVG 문자열
// 기능: PLANIT 화면에 사용되는 선형 아이콘을 반환한다.
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
// 기능: PLANIT 첫 진입 로그인 화면을 모바일 UI 보드 구성에 맞춰 표시한다.
function renderLogin() {
  const passwordType = state.passwordVisibility.login ? "text" : "password";

  return `
    <section class="screen auth-screen login-screen">
      <div class="brand-block">
        <p class="brand-name">PLAN<span>IT</span></p>
        <h1>일정을 더 쉽게<br />시작해요</h1>
        <p>사진 한 장으로 일정을 만들고,<br />캘린더에서 한눈에 확인해요.</p>
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
        <button class="forgot-button" type="button">비밀번호 찾기</button>
      </form>
      <div class="social-divider"><span></span><em>또는</em><span></span></div>
      <div class="social-login"><button type="button">Google 로그인</button><button type="button">카카오 로그인</button></div>
      <p class="auth-footer">계정이 없나요?<button type="button" data-action="open-signup">회원가입</button></p>
      <small class="auth-legal">로그인하면 이용약관과 개인정보 처리방침에<br />동의하게 돼요.</small>
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
        <h2>회원가입</h2>
        <p>기본 정보를 입력해 주세요.</p>
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
        <div class="signup-section-label">학교와 학년</div>
        <label class="form-field"><span>학교</span><input name="school" type="text" placeholder="학교 이름을 검색해 주세요" value="${escapeAttribute(state.signup.school)}" /></label>
        <div class="grade-segments">${["1학년", "2학년", "3학년"].map((grade) => `<button class="${state.signup.grade === grade ? "is-selected" : ""}" type="button" data-action="select-grade" data-grade="${grade}">${grade}</button>`).join("")}</div>
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
// 출력값: 선택한 약관의 상세 팝업 HTML 문자열
// 기능: 서비스 또는 개인정보 필수 약관의 주요 내용을 가입 전에 확인하게 한다.
function renderTermsDetailDialog() {
  if (!state.termsDetail) return "";
  const isService = state.termsDetail === "service";
  return `<div class="sheet-scrim" data-action="close-terms-detail"></div><section class="terms-detail-dialog" role="dialog" aria-modal="true"><div class="sheet-title-row"><h2>${isService ? "서비스 이용약관" : "개인정보 수집·이용 동의"}</h2><button type="button" data-action="close-terms-detail">${icon("close")}</button></div><div class="terms-detail-copy">${isService ? `<h3>PLANIT 서비스 이용</h3><p>PLANIT은 이미지 속 일정 정보를 분석해 사용자가 확인·수정하고 캘린더에 등록하도록 돕습니다.</p><h3>분석 결과 확인</h3><p>이미지 분석 결과는 자동으로 확정되지 않으며, 등록 전 사용자가 내용과 날짜를 확인해야 합니다.</p>` : `<h3>수집 항목</h3><p>계정 식별과 일정 관리에 필요한 이름, 이메일 및 사용자가 입력한 일정 정보를 사용합니다.</p><h3>이용 목적</h3><p>회원 기능, 일정 관리, 알림과 그룹 공유 기능 제공을 위해 사용합니다.</p>`}</div><button class="primary-button" type="button" data-action="agree-detail-term" data-term="${state.termsDetail}">확인하고 동의</button></section>`;
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
          <button class="term-detail" type="button" data-action="open-terms-detail" data-term="service" aria-label="서비스 이용약관 상세">${icon("chevron")}</button>
        </div>
        <div class="term-row">
          <button class="term-main" type="button" data-action="toggle-term" data-term="privacy">${renderTermCheckbox(state.terms.privacy)}<span><strong>[필수]</strong> 개인정보 수집·이용 동의</span></button>
          <button class="term-detail" type="button" data-action="open-terms-detail" data-term="privacy" aria-label="개인정보 수집 이용 동의 상세">${icon("chevron")}</button>
        </div>
        <div class="term-row">
          <button class="term-main" type="button" data-action="toggle-term" data-term="notification">${renderTermCheckbox(state.terms.notification)}<span><b>[선택]</b> 일정 알림 수신 동의</span></button>
        </div>
      </div>

      <button class="primary-button terms-submit" type="button" data-action="complete-signup" ${requiredChecked ? "" : "disabled"}>가입 완료</button>
      ${renderTermsDetailDialog()}
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
    .map((schedule) => `<button class="board-schedule-row" type="button" data-action="open-event" data-event-id="${schedule.id}"><i class="schedule-accent ${schedule.type === "group" ? "is-group" : ""}"></i><span class="board-schedule-copy"><strong>${escapeAttribute(schedule.title)}</strong><small><em>${schedule.type === "group" ? icon("users") : icon("user")}${schedule.type === "group" ? "그룹" : "개인"}</em>${escapeAttribute(schedule.meta)}</small></span><time>${schedule.time}</time></button>`)
    .join("");
}

// 입력값: 선택한 날짜의 마감 일정 목록
// 출력값: 마감 일정 카드 HTML 문자열
// 기능: 선택한 날짜의 마감 일정 또는 빈 상태를 표시한다.
function renderDeadlines(deadlines) {
  if (deadlines.length === 0) {
    return `<div class="deadline-empty">마감 일정이 없어요</div>`;
  }

  return `<div class="deadline-card">${deadlines.map((deadline) => `<p><strong>${escapeAttribute(deadline.title)}</strong><span>${deadline.due}</span></p>`).join("")}</div>`;
}

// 입력값: YYYY-MM-DD 형식의 홈 날짜 문자열
// 출력값: 고정 수업과 사용자가 임시 등록한 일정을 합친 홈 화면 데이터
// 기능: DB 없이 등록한 일정도 현재 탭을 나가기 전까지 홈에서 바로 확인하게 한다.
function getHomeContent(dateKey) {
  const [year, month, day] = dateKey.split("-").map(Number);
  const selectedDate = new Date(year, month - 1, day, 12);
  const weekday = ["일", "월", "화", "수", "목", "금", "토"][selectedDate.getDay()];
  const schedules = state.events
    .filter((eventItem) => eventItem.date === dateKey)
    .map((eventItem) => ({
      id: eventItem.id,
      time: eventItem.allDay ? "종일" : eventItem.start,
      title: eventItem.title,
      meta: eventItem.location || eventItem.category,
      type: eventItem.category === "그룹" || eventItem.shared ? "group" : "personal",
    }));

  return {
    heading: `${month}월 ${day}일 ${weekday}요일`,
    deadlines: schedules.filter((schedule) => /제출|평가|마감/.test(schedule.title)).map((schedule) => ({ title: schedule.title, due: `${schedule.time}까지` })),
    schedules: schedules.sort((first, second) => first.time.localeCompare(second.time)),
  };
}

// 입력값: YYYY-MM-DD 형식의 기준 날짜 문자열
// 출력값: 기준 날짜가 포함된 일요일부터 토요일까지의 날짜 정보 배열
// 기능: 사용자의 기기 날짜를 중심으로 메인 화면의 주간 날짜를 자동 생성한다.
function getHomeWeekDates(dateKey) {
  const [year, month, day] = dateKey.split("-").map(Number);
  const referenceDate = new Date(year, month - 1, day, 12);
  const weekStart = new Date(referenceDate);
  weekStart.setDate(referenceDate.getDate() - referenceDate.getDay());

  return Array.from({ length: 7 }, (_, index) => {
    const currentDate = new Date(weekStart);
    currentDate.setDate(weekStart.getDate() + index);
    return {
      dateKey: toDateKey(currentDate.getFullYear(), currentDate.getMonth() + 1, currentDate.getDate()),
      dayNumber: String(currentDate.getDate()),
      weekday: ["일", "월", "화", "수", "목", "금", "토"][currentDate.getDay()],
      isSunday: currentDate.getDay() === 0,
    };
  });
}

// 입력값: 현재 활성화할 메뉴 이름
// 출력값: 공통 하단 내비게이션 HTML 문자열
// 기능: 홈, 시간표, 캘린더, 마이 화면을 오가는 고정 메뉴를 표시한다.
function renderBottomNav(activeView) {
  const navItems = [
    { view: "home", label: "홈", iconName: "home" },
    { view: "calendar", label: "캘린더", iconName: "calendar" },
    { action: "new-event", label: "일정 등록", iconName: "plus", isCreate: true },
    { view: "groups", label: "그룹", iconName: "users" },
    { view: "settings", label: "설정", iconName: "gear" },
  ];

  return `<nav class="bottom-nav" aria-label="주요 메뉴">${navItems
    .map(({ view, action = "navigate", label, iconName, isCreate = false }) => `<button class="nav-item ${activeView === view ? "is-active" : ""} ${isCreate ? "is-create" : ""}" type="button" data-action="${action}" ${view ? `data-view="${view}"` : ""} aria-label="${label}">${icon(iconName)}<span>${label}</span></button>`)
    .join("")}</nav>`;
}

// 입력값: 없음
// 출력값: 메인 홈 화면 HTML 문자열
// 기능: 오늘 일정, 주간 날짜, 다가오는 일정과 빠른 메뉴를 표시한다.
function renderHome() {
  const filters = [
    ["all", "전체"],
    ["personal", "개인"],
    ["group", "그룹"],
  ];
  const selectedContent = getHomeContent(state.selectedDate);
  const todayContent = getHomeContent(deviceTodayKey);
  const dates = getHomeWeekDates(deviceTodayKey);
  const nextSchedule = todayContent.schedules[0];
  const todayDateText = formatKoreanDate(deviceTodayKey).replace(/^\d+년 /, "");

  return `
    <section class="screen home-screen ${state.addMenuOpen ? "has-menu" : ""}">
      <div class="home-scroll">
        <header class="board-home-header"><strong class="wordmark">PLANIT</strong><div><button class="home-add-button" type="button" data-action="new-event">${icon("plus")}<span>일정 등록</span></button><button class="home-icon-button" type="button" data-action="toggle-search" aria-label="검색">${icon("search")}</button><button class="home-icon-button" type="button" data-action="navigate" data-view="notifications" aria-label="알림">${icon("bell")}<i></i></button></div></header>

        <p class="today-label">오늘 · ${todayDateText}</p>
        <h1 class="home-greeting">안녕하세요, 서희님!</h1>

        <section class="today-summary-card">
          <p>${todayContent.heading}</p>
          <h2>오늘의 일정</h2>
          <div class="today-count"><strong>${todayContent.schedules.length}</strong><span>개의 일정이 있습니다!</span></div>
          ${nextSchedule ? `<button type="button" data-action="open-event" data-event-id="${nextSchedule.id}"><span>다음 · ${nextSchedule.time} ${escapeAttribute(nextSchedule.title)}</span>${icon("chevron")}</button>` : `<button type="button" data-action="new-event"><span>첫 일정을 추가해 보세요</span>${icon("plus")}</button>`}
        </section>

        <div class="date-strip" aria-label="이번 주 날짜">
          ${dates.map(({ dateKey, dayNumber, weekday, isSunday }) => {
            const hasEvents = getHomeContent(dateKey).schedules.length > 0;
            const isToday = dateKey === deviceTodayKey;
            return `<button class="date-item ${dateKey === state.selectedDate ? "is-selected" : ""} ${isToday ? "is-today" : ""} ${isSunday ? "is-sunday" : ""} ${hasEvents ? "has-events" : ""}" type="button" data-action="select-date" data-date="${dateKey}" aria-pressed="${dateKey === state.selectedDate}" ${isToday ? 'aria-current="date"' : ""}><small>${weekday}</small><strong>${dayNumber}</strong></button>`;
          }).join("")}
        </div>

        <section class="schedule-section">
          <div class="schedule-heading"><h2>다가오는 일정</h2><button type="button" data-action="navigate" data-view="calendar">전체 보기</button></div>
          <div class="schedule-filters">${filters.map(([value, label]) => `<button class="${state.homeFilter === value ? "is-active" : ""}" type="button" data-action="set-filter" data-filter="${value}">${label}</button>`).join("")}</div>
          <div class="schedule-list">${renderScheduleItems(selectedContent.schedules, state.homeFilter)}</div>
        </section>

        <section class="home-shortcuts">
          <button class="is-salmon" type="button" data-action="navigate" data-view="upload">${icon("image")}<strong>안내문 사진으로<br />일정 만들기</strong><small>사진 속 날짜를 찾아드려요</small></button>
          <button type="button" data-action="navigate" data-view="timetable">${icon("timetable")}<strong>시간표</strong><small>오늘 6교시</small></button>
        </section>
      </div>
      ${renderBottomNav("home")}
    </section>
  `;
}

// 입력값: 수업 정보 또는 빈 값
// 출력값: 없음
// 기능: 새 수업 또는 기존 수업의 값을 수업 편집 상태에 준비한다.
function openClassEditor(classItem = null) {
  state.editingClassId = classItem?.id ?? null;
  state.classColorChanged = false;
  state.classDraft = classItem
    ? { day: classItem.day, period: String(classItem.period), title: classItem.title, teacher: classItem.teacher, room: classItem.room, color: classItem.color }
    : { day: state.selectedWeekday, period: "1", title: "", teacher: "", room: "", color: "violet" };
  state.classEditorOpen = true;
}

// 입력값: 과목명과 적용할 색상 이름
// 출력값: 없음
// 기능: 요일과 교시가 달라도 이름이 같은 모든 과목의 색상을 한 번에 맞춘다.
function applySubjectColor(subjectTitle, color) {
  const normalizedTitle = subjectTitle.trim().replace(/\s+/g, " ").toLocaleLowerCase("ko-KR");
  state.timetable.forEach((classItem) => {
    const itemTitle = classItem.title.trim().replace(/\s+/g, " ").toLocaleLowerCase("ko-KR");
    if (itemTitle === normalizedTitle) classItem.color = color;
  });
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
          <label class="compact-field"><span>교시</span><select name="period">${state.periodTimes.map(({ period, label }) => `<option value="${period}" ${String(period) === state.classDraft.period ? "selected" : ""}>${label || `${period}교시`}</option>`).join("")}</select></label>
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

// 입력값: 조회·청소·종례 시간 정보
// 출력값: 시간표에 표시할 시간 문자열
// 기능: 종료 시간이 없는 종례와 대체 시간이 있는 일과를 원본 표 형식으로 만든다.
function formatRoutineTime(routine) {
  const mainTime = routine.end ? `${routine.start}~${routine.end}` : `${routine.start}~`;
  return routine.note ? `${mainTime} (${routine.note})` : mainTime;
}

// 입력값: 교시 번호
// 출력값: 교시 또는 방과후 표시 문자열
// 기능: 시간표와 설정 목록에서 같은 교시 이름을 사용한다.
function getTimetablePeriodLabel(period) {
  const periodItem = state.periodTimes.find((item) => item.period === period);
  return periodItem?.label || `${period}교시`;
}

// 입력값: 교시 정보와 평일 목록
// 출력값: 한 교시의 시간 및 월~금 수업 셀 HTML 문자열
// 기능: 정규 교시와 방과후 수업을 같은 격자 규칙으로 표시한다.
function renderTimetablePeriodRow(periodItem, weekdays) {
  const { period, start, label, isAfterSchool } = periodItem;
  const periodLabel = label || `${period}교시`;
  const compactLabel = label || String(period);
  const periodClass = isAfterSchool ? " is-after-school" : "";
  const cells = weekdays.map((day) => {
    const classItem = state.timetable.find((item) => item.day === day && item.period === period);
    const activeDayClass = day === state.selectedWeekday ? " is-active-day" : "";
    if (!classItem) {
      return `<button class="timetable-cell is-empty${activeDayClass}${periodClass}" type="button" data-action="add-class-for-slot" data-day="${day}" data-period="${period}" aria-label="${day}요일 ${periodLabel} 수업 추가">${icon("plus")}</button>`;
    }
    return `<button class="timetable-cell color-${classItem.color}${activeDayClass}${periodClass}" type="button" data-action="edit-class" data-class-id="${classItem.id}" data-day="${day}"><strong>${escapeAttribute(classItem.title)}</strong>${classItem.room ? `<small>${escapeAttribute(classItem.room)}</small>` : ""}</button>`;
  }).join("");
  const lunch = period === 4 ? `<div class="timetable-lunch">점심시간 ${state.lunchTime.start} – ${state.lunchTime.end}</div>` : "";
  return `<div class="timetable-time${periodClass}" aria-label="${periodLabel}"><strong>${compactLabel}</strong><small>${start}</small></div>${cells}${lunch}`;
}

// 입력값: 없음
// 출력값: 월요일부터 금요일까지의 전체 시간표 격자 HTML 문자열
// 기능: 1교시부터 방과후까지 모든 요일을 빠짐없이 학교 시간표 구조로 표시한다.
function renderTimetableClasses() {
  const weekdays = ["월", "화", "수", "목", "금"];
  const header = `<span class="timetable-corner"></span>${weekdays.map((day) => `<button class="timetable-weekday ${day === state.selectedWeekday ? "is-active" : ""}" type="button" data-action="select-weekday" data-day="${day}" aria-pressed="${day === state.selectedWeekday}">${day}</button>`).join("")}`;
  const morning = `<div class="timetable-routine"><strong>${state.routineTimes.morning.label}</strong><span>${formatRoutineTime(state.routineTimes.morning)}</span></div>`;
  const regularRows = state.periodTimes.filter((period) => !period.isAfterSchool).map((period) => renderTimetablePeriodRow(period, weekdays)).join("");
  const afterSchoolRows = state.periodTimes.filter((period) => period.isAfterSchool).map((period) => renderTimetablePeriodRow(period, weekdays)).join("");
  const cleaning = `<div class="timetable-routine"><strong>${state.routineTimes.cleaning.label}</strong><span>${formatRoutineTime(state.routineTimes.cleaning)}</span></div>`;
  const homeroom = `<div class="timetable-routine"><strong>${state.routineTimes.homeroom.label}</strong><span>${formatRoutineTime(state.routineTimes.homeroom)}</span></div>`;
  return `<div class="timetable-grid">${header}${morning}${regularRows}${cleaning}${homeroom}${afterSchoolRows}</div>`;
}

// 입력값: 없음
// 출력값: 내 시간표 화면 HTML 문자열
// 기능: 학급의 월~금 전체 시간표와 시간표 편집 진입 기능을 표시한다.
function renderTimetable() {
  return `
    <section class="screen timetable-screen">
      <header class="timetable-header"><div><h1>시간표</h1><p>과목을 누르면 색상을 바꿀 수 있어요</p></div><span class="timetable-actions"><button type="button" data-action="open-timetable-settings" aria-label="교시 시간 설정">${icon("clock")}</button><button type="button" data-action="add-class" aria-label="과목 추가">${icon("plus")}</button></span></header>
      <div class="timetable-grid-wrap">${renderTimetableClasses()}</div>
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
    return `<section class="settings-day"><h3>${day}요일</h3>${classes.length ? classes.map((item) => `<div class="settings-class color-${item.color}"><button type="button" data-action="edit-class" data-class-id="${item.id}"><strong>${getTimetablePeriodLabel(item.period)} · ${escapeAttribute(item.title)}</strong>${item.teacher || item.room ? `<small>${escapeAttribute([item.teacher, item.room].filter(Boolean).join(" · "))}</small>` : ""}</button><button class="row-delete" type="button" data-action="delete-class" data-class-id="${item.id}" aria-label="${escapeAttribute(item.title)} 삭제">${icon("trash")}</button></div>`).join("") : `<p class="settings-empty">등록된 수업이 없어요</p>`}<button class="inline-add" type="button" data-action="add-class-for-day" data-day="${day}">${icon("plus")} 과목 추가</button></section>`;
  }).join("")}</div>`;
}

// 입력값: 없음
// 출력값: 없음
// 기능: 저장된 교시·점심시간을 사용자가 수정할 임시 상태로 복사한다.
function prepareTimetableTimeDraft() {
  state.timetableTimeDraft = {
    periods: state.periodTimes.map((period) => ({ ...period })),
    lunch: { ...state.lunchTime },
    routines: Object.fromEntries(Object.entries(state.routineTimes).map(([key, routine]) => [key, { ...routine }])),
  };
  state.timetableTimeError = "";
}

// 입력값: 교시 및 점심시간 임시 설정
// 출력값: 모든 종료 시간이 시작 시간보다 늦은지 여부
// 기능: 잘못된 시간 범위가 시간표에 적용되지 않도록 검증한다.
function isTimetableTimeDraftValid(draft) {
  const validRoutines = Object.values(draft.routines).every((routine) => !routine.end || routine.start < routine.end);
  return draft.periods.every((period) => period.start < period.end) && draft.lunch.start < draft.lunch.end && validRoutines;
}

// 입력값: 생활 일정 키와 시간 정보
// 출력값: 조회·청소·종례 시간 설정 행 HTML 문자열
// 기능: 정규 교시 외 학교 일과도 직접 설정할 수 있는 입력 행을 만든다.
function renderRoutineSettingRow(key, routine) {
  const optionalLabel = key === "homeroom" ? "(선택)" : "";
  return `<div class="period-setting-row routine-setting-row"><strong>${routine.label}</strong><label><span>시작</span><input type="time" data-time-kind="routine" data-routine="${key}" data-time-field="start" value="${routine.start}" /></label><i>—</i><label><span>종료${optionalLabel}</span><input type="time" data-time-kind="routine" data-routine="${key}" data-time-field="end" value="${routine.end}" /></label></div>`;
}

// 입력값: 없음
// 출력값: 교시 시간 설정 HTML 문자열
// 기능: 조회, 각 교시, 점심, 청소, 종례와 방과후 시간을 직접 입력하고 시간표에 적용하게 한다.
function renderPeriodSettings() {
  const draft = state.timetableTimeDraft ?? { periods: state.periodTimes, lunch: state.lunchTime, routines: state.routineTimes };
  const lunchRow = `<div class="period-setting-row lunch-setting-row"><strong>점심</strong><label><span>시작</span><input type="time" data-time-kind="lunch" data-time-field="start" value="${draft.lunch.start}" /></label><i>—</i><label><span>종료</span><input type="time" data-time-kind="lunch" data-time-field="end" value="${draft.lunch.end}" /></label></div>`;
  const regularPeriodRows = draft.periods.filter((period) => !period.isAfterSchool).map(({ period, start, end, label }) => `<div class="period-setting-row"><strong>${label || `${period}교시`}</strong><label><span>시작</span><input type="time" data-time-kind="period" data-period="${period}" data-time-field="start" value="${start}" /></label><i>—</i><label><span>종료</span><input type="time" data-time-kind="period" data-period="${period}" data-time-field="end" value="${end}" /></label></div>${period === 4 ? lunchRow : ""}`).join("");
  const afterSchoolRows = draft.periods.filter((period) => period.isAfterSchool).map(({ period, start, end, label }) => `<div class="period-setting-row after-school-setting-row"><strong>${label || "방과후"}</strong><label><span>시작</span><input type="time" data-time-kind="period" data-period="${period}" data-time-field="start" value="${start}" /></label><i>—</i><label><span>종료</span><input type="time" data-time-kind="period" data-period="${period}" data-time-field="end" value="${end}" /></label></div>`).join("");
  return `<div class="settings-content period-settings"><div class="time-settings-guide"><strong>수업 시간을 설정해 주세요</strong><p>조회부터 방과후까지 학교 시간에 맞게 직접 바꿀 수 있어요.</p></div>${renderRoutineSettingRow("morning", draft.routines.morning)}${regularPeriodRows}${renderRoutineSettingRow("cleaning", draft.routines.cleaning)}${renderRoutineSettingRow("homeroom", draft.routines.homeroom)}${afterSchoolRows}${state.timetableTimeError ? `<p class="time-settings-error">${icon("warning")}${state.timetableTimeError}</p>` : ""}<button class="primary-button time-settings-submit" type="button" data-action="apply-timetable-times">시간표에 적용</button></div>`;
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

// 입력값: 완료 뒤 돌아갈 화면
// 출력값: 새 일정에 사용할 YYYY-MM-DD 형식의 날짜 문자열
// 기능: 캘린더에서는 선택 날짜를, 나머지 화면에서는 기기 오늘 날짜를 기본값으로 연결한다.
function getDefaultEventDate(returnView) {
  if (returnView === "calendar") {
    return toDateKey(state.calendarYear, state.calendarMonth, state.calendarSelectedDay);
  }
  return deviceTodayKey;
}

// 입력값: 일정 객체 또는 빈 값, 완료 뒤 돌아갈 화면
// 출력값: 없음
// 기능: 화면 맥락에 맞는 날짜로 새 일정을 준비하거나 기존 일정을 편집한다.
function openEventForm(eventItem = null, returnView = "calendar") {
  state.eventFormMode = eventItem ? "edit" : "create";
  state.eventReturnView = returnView;
  state.eventFormError = "";
  state.eventDraft = eventItem
    ? { title: eventItem.title, category: eventItem.category, date: eventItem.date, start: eventItem.start, end: eventItem.end, allDay: eventItem.allDay, location: eventItem.location, preparation: eventItem.preparation || "", memo: eventItem.memo, alarm: eventItem.alarm, repeat: eventItem.repeat || "반복 안 함", sharedGroup: eventItem.sharedGroup || "공유 안 함" }
    : { title: "", category: "개인", date: getDefaultEventDate(returnView), start: "09:00", end: "10:00", allDay: false, location: "", preparation: "", memo: "", alarm: "10분 전", repeat: "반복 안 함", sharedGroup: "공유 안 함" };
  state.dateTimePickerOpen = false;
  state.alarmPickerOpen = false;
  state.currentView = "eventForm";
}

// 입력값: 일정 종류
// 출력값: 카드에 적용할 종류별 클래스 이름
// 기능: 개인·과제·학교·그룹 일정을 색상으로 구분한다.
function getEventTone(category) {
  const tones = { 개인: "pink", 과제: "pink", 학교: "blue", 그룹: "gray" };
  return tones[category] ?? "pink";
}

// 입력값: 없음
// 출력값: 현재 월의 달력 셀 HTML 문자열
// 기능: 이전·현재·다음 달 날짜를 포함한 6주 달력을 생성하고 일정 유무를 표시한다.
function renderCalendarGrid() {
  const firstDay = new Date(state.calendarYear, state.calendarMonth - 1, 1).getDay();
  const daysInMonth = new Date(state.calendarYear, state.calendarMonth, 0).getDate();
  const daysInPreviousMonth = new Date(state.calendarYear, state.calendarMonth - 1, 0).getDate();
  const cells = [];

  const cellCount = Math.ceil((firstDay + daysInMonth) / 7) * 7;
  for (let index = 0; index < cellCount; index += 1) {
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
    .map((item) => `<button class="calendar-event color-${getEventTone(item.category)}" type="button" data-action="open-event" data-event-id="${item.id}"><span class="event-time">${item.allDay ? "종일" : item.start}</span><span class="event-summary"><strong>${escapeAttribute(item.title)}</strong><small>${escapeAttribute(item.location || item.category)}${item.shared ? " · 공유 일정" : ""}</small></span>${icon("chevron")}</button>`)
    .join("");
}

// 입력값: 없음
// 출력값: 일정 캘린더 화면 HTML 문자열
// 기능: 월간 달력과 선택한 날짜의 일정 목록을 함께 표시한다.
function renderCalendar() {
  const dateKey = toDateKey(state.calendarYear, state.calendarMonth, state.calendarSelectedDay);
  const selectedEvents = state.events.filter((item) => item.date === dateKey);
  const visibleEvents = state.calendarFilter === "all"
    ? selectedEvents
    : selectedEvents.filter((item) => state.calendarFilter === "group" ? item.category === "그룹" || item.shared : item.category !== "그룹" && !item.shared);
  const selectedDateText = formatKoreanDate(dateKey).replace(`${state.calendarYear}년 `, "");
  return `
    <section class="screen calendar-screen">
      <div class="calendar-scroll">
        <header class="calendar-board-header"><button class="calendar-year" type="button" data-action="change-month" data-offset="-1">${icon("back")} ${state.calendarYear}년</button><div><button type="button" data-action="toggle-search" aria-label="일정 검색">${icon("search")}</button><button type="button" data-action="new-event" aria-label="일정 추가">${icon("plus")}</button></div></header>
        <h1 class="calendar-month-title">${state.calendarMonth}월</h1>
        <div class="calendar-weekdays"><span>일</span><span>월</span><span>화</span><span>수</span><span>목</span><span>금</span><span>토</span></div>
        <div class="calendar-grid">${renderCalendarGrid()}</div>
        <section class="calendar-agenda"><div class="agenda-heading"><h2>${selectedDateText}</h2><em>${selectedEvents.length}개</em></div><div class="segment-control calendar-segments"><button class="${state.calendarFilter === "all" ? "is-active" : ""}" type="button" data-action="set-calendar-filter" data-filter="all">전체</button><button class="${state.calendarFilter === "personal" ? "is-active" : ""}" type="button" data-action="set-calendar-filter" data-filter="personal">개인</button><button class="${state.calendarFilter === "group" ? "is-active" : ""}" type="button" data-action="set-calendar-filter" data-filter="group">그룹</button></div><div class="calendar-event-list">${renderCalendarEvents(visibleEvents)}</div></section>
      </div>
      ${renderBottomNav("calendar")}
    </section>`;
}

// 입력값: 아이콘 이름, 항목 제목, 항목 값
// 출력값: 일정 상세 정보 행 HTML 문자열
// 기능: 날짜·장소·메모·알림 정보를 같은 형식으로 표시한다.
function renderEventInfoRow(iconName, label, value) {
  return `<div class="event-info-row"><span class="event-info-icon">${icon(iconName)}</span><div><small>${label}</small><p>${escapeAttribute(value || "없음")}</p></div></div>`;
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
      <header class="board-page-header"><button type="button" data-action="navigate" data-view="${state.eventDetailReturnView}">${icon("back")}<span>캘린더</span></button><h1>일정 상세</h1><button class="round-more" type="button" data-action="open-delete-event" aria-label="일정 메뉴">${icon("more")}</button></header>
      <div class="event-detail-scroll">
        <div class="event-title-block"><p>${formatKoreanDate(eventItem.date).replace("2026년 ", "")}</p><h2>${escapeAttribute(eventItem.title)}</h2><span class="event-category color-${getEventTone(eventItem.category)}">${icon(eventItem.category === "그룹" ? "users" : "user")}${eventItem.category}</span></div>
        <div class="event-info-card">
          ${renderEventInfoRow("clock", "시간", timeText)}
          ${renderEventInfoRow("pin", "장소", eventItem.location)}
          ${renderEventInfoRow("note", "준비물", eventItem.preparation)}
        </div>
        <div class="event-info-card secondary-info-card">
          ${renderEventInfoRow("bell", "알림", eventItem.alarm)}
          ${renderEventInfoRow("note", "메모", eventItem.memo)}
        </div>
        <div class="event-info-card secondary-info-card">
          <button class="detail-setting-row" type="button" data-action="open-share-groups">${icon("share")}<span><small>공유 설정</small><strong>${escapeAttribute(eventItem.sharedGroup || (eventItem.shared ? "그룹 공유" : "공유 안 함"))}</strong></span>${icon("chevron")}</button>
          <div class="detail-setting-row">${icon("refresh")}<span><small>반복 설정</small><strong>${escapeAttribute(eventItem.repeat || "반복 안 함")}</strong></span></div>
        </div>
        <div class="detail-actions"><button class="primary-button" type="button" data-action="edit-event">수정하기</button><button class="delete-outline-button" type="button" data-action="open-delete-event">일정 삭제</button></div>
      </div>
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
  return `
    <section class="screen event-form-screen">
      <header class="board-page-header form-header"><button class="header-text-button" type="button" data-action="cancel-event-form">취소</button><h1>${isEditing ? "일정 수정" : "새로운 일정"}</h1><button class="header-text-button is-strong" type="submit" form="event-form">저장</button></header>
      <form id="event-form" class="event-form board-event-form" data-form="event" novalidate>
        <div class="segment-control event-type-segments" role="group" aria-label="일정 종류"><button class="${state.eventDraft.category !== "그룹" ? "is-active" : ""}" type="button" data-action="select-event-category" data-category="개인">${icon("user")}<span>개인 일정</span></button><button class="${state.eventDraft.category === "그룹" ? "is-active" : ""}" type="button" data-action="select-event-category" data-category="그룹">${icon("users")}<span>그룹 일정</span></button></div>
        <label class="board-input-card title-input"><span class="sr-only">일정 제목</span><input name="title" value="${escapeAttribute(state.eventDraft.title)}" placeholder="일정 제목" /></label>
        <section class="form-section-card date-section-card">
          <label class="switch-row"><span><strong>종일</strong></span><input type="checkbox" data-draft-field="allDay" ${state.eventDraft.allDay ? "checked" : ""} /></label>
          <label class="inline-form-row"><span>날짜</span><input type="date" data-draft-field="date" value="${state.eventDraft.date}" /></label>
          <label class="inline-form-row"><span>시작</span><input type="time" data-draft-field="start" value="${state.eventDraft.start}" ${state.eventDraft.allDay ? "disabled" : ""} /></label>
          <label class="inline-form-row"><span>종료</span><input type="time" data-draft-field="end" value="${state.eventDraft.end}" ${state.eventDraft.allDay ? "disabled" : ""} /></label>
        </section>
        ${state.eventFormError ? `<p class="event-form-error">${icon("warning")}${state.eventFormError}</p>` : ""}
        <section class="form-section-card text-section-card">
          <label><span>장소</span><input name="location" value="${escapeAttribute(state.eventDraft.location)}" placeholder="장소" /></label>
          <label><span>준비물</span><input name="preparation" value="${escapeAttribute(state.eventDraft.preparation)}" placeholder="준비물" /></label>
          <label><span>메모</span><textarea name="memo" placeholder="메모">${escapeAttribute(state.eventDraft.memo)}</textarea></label>
        </section>
        <section class="form-section-card option-section-card">
          <label class="inline-form-row"><span>반복</span><select name="repeat"><option ${state.eventDraft.repeat === "반복 안 함" ? "selected" : ""}>반복 안 함</option><option ${state.eventDraft.repeat === "매일" ? "selected" : ""}>매일</option><option ${state.eventDraft.repeat?.startsWith("매주") ? "selected" : ""}>매주 화요일</option></select></label>
          <label class="inline-form-row"><span>알림</span><select name="alarm">${["알림 없음", "정시", "10분 전", "30분 전", "1시간 전", "1일 전"].map((alarm) => `<option ${state.eventDraft.alarm === alarm ? "selected" : ""}>${alarm}</option>`).join("")}</select></label>
          <label class="inline-form-row"><span>공유 그룹</span><select name="sharedGroup"><option>공유 안 함</option>${state.groups.map((group) => `<option ${state.eventDraft.sharedGroup === group.name ? "selected" : ""}>${escapeAttribute(group.name)}</option>`).join("")}</select></label>
        </section>
        <p class="form-help">개인 일정은 공유하지 않으면 그룹에 보이지 않아요.</p>
        <button class="primary-button event-submit" type="submit" ${state.eventDraft.title.trim() ? "" : "disabled"}>${isEditing ? "변경사항 저장" : "저장하기"}</button>
        <button class="form-cancel-button" type="button" data-action="cancel-event-form">취소</button>
      </form>
    </section>`;
}

// 입력값: 없음
// 출력값: 분석 예시 일정 후보 배열
// 기능: 교체 가능한 이미지 분석 단계의 정적 데모 결과를 만든다.
function createExtractionCandidates() {
  return [
    { id: 201, selected: true, title: "국어 독서 감상문", category: "개인", date: "2026-04-16", start: "09:00", end: "10:00", allDay: false, location: "", preparation: "", memo: "", alarm: "1일 전", repeat: "반복 안 함", sharedGroup: "공유 안 함", confidence: "확인 완료", uncertain: [] },
    { id: 202, selected: true, title: "수학 개념 탐구 보고서", category: "개인", date: "2026-04-22", start: "09:00", end: "10:00", allDay: false, location: "", preparation: "보고서", memo: "날짜를 다시 확인해 주세요.", alarm: "1일 전", repeat: "반복 안 함", sharedGroup: "공유 안 함", confidence: "확인이 필요해요", uncertain: ["날짜"] },
    { id: 203, selected: true, title: "영어 단원별 발표", category: "개인", date: "2026-04-25", start: "09:00", end: "10:00", allDay: false, location: "", preparation: "발표 자료", memo: "", alarm: "1일 전", repeat: "반복 안 함", sharedGroup: "공유 안 함", confidence: "직접 수정됨", uncertain: [] },
  ];
}

// 입력값: 없음
// 출력값: 이미지 파일 선택 영역 HTML 문자열
// 기능: JPEG·PNG 파일을 선택하거나 드래그할 수 있는 첫 업로드 상태를 표시한다.
function renderUploadDropzone() {
  return `<div class="upload-choice"><label class="upload-dropzone" for="notice-image"><span class="upload-icon">${icon("image")}</span><strong>안내문을 올려 주세요</strong><p>가정통신문, 시간표, 공지 사진도 좋아요</p><input id="notice-image" type="file" accept="image/jpeg,image/png" /></label><div class="upload-button-row"><label class="primary-button" for="notice-image">${icon("image")} 이미지 선택</label><button type="button" data-action="open-camera">${icon("camera")} 카메라로 촬영</button></div></div>`;
}

// 입력값: 없음
// 출력값: 선택한 이미지 미리보기 HTML 문자열
// 기능: 분석 전에 파일명과 원본 이미지를 확인하고 다시 선택할 수 있게 한다.
function renderUploadPreview() {
  return `<div class="upload-preview"><div class="preview-image-wrap"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="선택한 안내문 미리보기" /></div><div class="preview-file-row"><span>${icon("image")}</span><div><strong>${escapeAttribute(state.upload.fileName)}</strong><small>${(state.upload.fileSize / 1024 / 1024).toFixed(1)}MB · 확인 완료</small></div><label for="notice-image">다시 선택<input id="notice-image" type="file" accept="image/jpeg,image/png" /></label></div><button class="primary-button" type="button" data-action="analyze-image">이미지 분석하기</button></div>`;
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
  if (state.upload.status === "analyzing") content = `<div class="analyzing-state"><div class="document-preview-card"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="분석 중인 안내문" /><i></i></div><small>${escapeAttribute(state.upload.fileName)}</small><h2>일정 정보를 찾고 있어요</h2><p>잠시만 기다려 주세요</p><div class="analysis-steps"><div class="is-complete"><span>${icon("check")}</span><strong>이미지 확인 완료</strong><em>완료</em></div><div class="is-progress"><span></span><strong>날짜와 시간 찾는 중</strong><em>진행 중</em></div><div><span></span><strong>일정 내용 정리</strong><em>대기</em></div></div><button class="analysis-cancel" type="button" data-action="reset-upload">분석 취소</button></div>`;
  if (state.upload.status === "error") content = renderAnalysisMessage("이미지를 분석하지 못했어요", state.upload.error || "이미지가 선명한지 확인하고 다시 시도해 주세요.", "warning");
  if (state.upload.status === "empty") content = renderAnalysisMessage("찾은 일정이 없어요", "날짜와 일정 내용이 잘 보이는 안내문으로 다시 시도해 주세요.", "calendar");

  if (state.upload.status === "analyzing") return `<section class="screen upload-screen analyzing-screen"><header class="board-page-header"><button type="button" data-action="reset-upload">${icon("back")}<span>이미지</span></button><h1>이미지 분석 중</h1><span></span></header><div class="upload-content">${content}</div></section>`;
  return `<section class="screen upload-screen"><div class="upload-scroll"><header class="upload-brand">PLANIT</header><div class="upload-content"><div class="upload-copy"><h1>이미지로<br />일정 만들기</h1><p>사진 속 날짜와 일정을 찾아드릴게요.</p></div>${content}<section class="upload-tips"><h2>이렇게 찍으면 더 정확해요</h2><p>${icon("check")} 안내문 전체가 보이게 촬영해요</p><p>${icon("check")} 날짜와 일정이 선명하게 보여야 해요</p><p>${icon("check")} 글자가 흐리지 않아야 해요</p><p>${icon("check")} 한 이미지의 여러 일정도 함께 분석해요</p></section><small class="upload-footnote">저장 전에 직접 확인하고 수정할 수 있어요.</small></div></div>${renderBottomNav("upload")}</section>`;
}

// 입력값: 없음
// 출력값: 카메라 권한 안내 화면 HTML 문자열
// 기능: 브라우저 카메라를 열기 전에 촬영 목적과 갤러리 대안을 안내한다.
function renderCameraPermission() {
  return `<section class="screen camera-permission-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="upload">${icon("back")}<span>이미지</span></button><h1>카메라로 촬영</h1><span></span></header><div class="permission-content"><span class="permission-icon">${icon("camera")}</span><h2>카메라 접근을<br />허용해 주세요</h2><p>안내문을 바로 촬영하려면<br />카메라 권한이 필요해요.</p><button class="primary-button" type="button" data-action="allow-camera">설정에서 허용하기</button><label class="secondary-button" for="permission-gallery">갤러리에서 선택하기<input id="permission-gallery" type="file" accept="image/jpeg,image/png" /></label></div></section>`;
}

// 입력값: 없음
// 출력값: 안내문 촬영 화면 HTML 문자열
// 기능: 모바일 카메라 입력을 열고 갤러리·플래시·전환 조작을 시각적으로 제공한다.
function renderCamera() {
  return `<section class="screen camera-screen"><header class="camera-header"><button type="button" data-action="navigate" data-view="upload">${icon("close")}</button><h1>카메라로 촬영</h1><button type="button" aria-label="플래시">${icon("flash")}</button></header><div class="camera-viewfinder"><div class="camera-document"><strong>4월 학사 일정 안내</strong><i></i><i></i><p>4월 16일 국어 독서 감상문 제출</p><p>4월 22일 수학 개념 탐구 보고서</p><p>4월 25일 영어 단원별 발표</p></div><span class="camera-guide">안내문 전체가 보이게 맞춰 주세요</span><b class="corner top-left"></b><b class="corner top-right"></b><b class="corner bottom-left"></b><b class="corner bottom-right"></b></div><div class="camera-controls"><label for="camera-gallery" aria-label="갤러리에서 선택">${icon("image")}<input id="camera-gallery" type="file" accept="image/jpeg,image/png" /></label><label class="camera-shutter" for="camera-image" aria-label="사진 촬영"><i></i><input id="camera-image" type="file" accept="image/*" capture="environment" /></label><button type="button" aria-label="카메라 전환">${icon("refresh")}</button></div><p class="camera-mode"><strong>문서</strong><span>사진</span></p></section>`;
}

// 입력값: 없음
// 출력값: 촬영 이미지 확인 화면 HTML 문자열
// 기능: 선택한 사진의 가독성을 확인하고 분석 또는 재촬영으로 이동하게 한다.
function renderCameraReview() {
  return `<section class="screen camera-review-screen"><header class="board-page-header"><button type="button" data-action="retake-photo">${icon("back")}<span>다시 찍기</span></button><h1>사진 확인</h1><span></span></header><div class="camera-review-scroll"><div class="review-image"><img src="${escapeAttribute(state.upload.previewUrl)}" alt="촬영한 안내문" /></div><h2>이렇게 보여요</h2><div class="review-checks"><p>${icon("check")} 안내문 전체가 보여요</p><p>${icon("check")} 글자가 선명해요</p></div><small>날짜와 일정이 잘 읽히는지 확인해 주세요.</small><button class="primary-button" type="button" data-action="use-camera-photo">이 사진으로 분석하기</button><button class="secondary-button" type="button" data-action="retake-photo">다시 찍기</button></div></section>`;
}

// 입력값: 분석된 일정 후보 객체
// 출력값: 선택·수정 가능한 일정 후보 카드 HTML 문자열
// 기능: AI가 추출한 각 일정과 확인이 필요한 필드를 구분해 표시한다.
function renderCandidateCard(candidate) {
  const timeText = candidate.allDay ? "종일" : `${candidate.start} - ${candidate.end}`;
  return `<article class="candidate-card ${candidate.selected ? "is-selected" : ""}"><button class="candidate-check" type="button" data-action="toggle-candidate" data-candidate-id="${candidate.id}" aria-label="${escapeAttribute(candidate.title)} 선택">${candidate.selected ? icon("check") : ""}</button><div class="candidate-main"><div class="candidate-title-row"><span class="event-category color-${getEventTone(candidate.category)}">${candidate.category}</span><em class="${candidate.uncertain.length ? "needs-check" : ""}">${candidate.confidence}</em></div><h3>${escapeAttribute(candidate.title)}</h3><p>${formatKoreanDate(candidate.date)}</p><p>${timeText}${candidate.location ? ` · ${escapeAttribute(candidate.location)}` : ""}</p>${candidate.uncertain.length ? `<small>${icon("warning")} ${candidate.uncertain.join(", ")}을 확인해 주세요</small>` : ""}</div><button class="candidate-edit" type="button" data-action="edit-candidate" data-candidate-id="${candidate.id}" aria-label="${escapeAttribute(candidate.title)} 수정">${icon("pencil")}</button></article>`;
}

// 입력값: 없음
// 출력값: 추출 일정 수정 바텀시트 HTML 문자열
// 기능: 분석 후보의 모호하거나 잘못된 정보를 등록 전에 직접 수정한다.
function renderCandidateEditor() {
  if (!state.candidateDraft) return "";
  return `<div class="sheet-scrim" data-action="close-candidate-editor"></div><section class="bottom-sheet candidate-editor-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>추출 일정 수정</h2><button type="button" data-action="close-candidate-editor">${icon("close")}</button></div><form class="candidate-form" data-form="candidate"><label class="compact-field"><span>일정 제목</span><input name="title" value="${escapeAttribute(state.candidateDraft.title)}" /></label><div class="field-pair"><label class="compact-field"><span>날짜</span><input type="date" name="date" value="${state.candidateDraft.date}" /></label><label class="compact-field"><span>종류</span><select name="category">${["개인", "과제", "학교", "그룹"].map((category) => `<option ${category === state.candidateDraft.category ? "selected" : ""}>${category}</option>`).join("")}</select></label></div><div class="field-pair"><label class="compact-field"><span>시작</span><input type="time" name="start" value="${state.candidateDraft.start}" /></label><label class="compact-field"><span>종료</span><input type="time" name="end" value="${state.candidateDraft.end}" /></label></div><label class="compact-field"><span>장소</span><input name="location" value="${escapeAttribute(state.candidateDraft.location)}" /></label><label class="compact-field"><span>메모</span><textarea name="memo">${escapeAttribute(state.candidateDraft.memo)}</textarea></label><button class="primary-button" type="submit">수정 완료</button></form></section>`;
}

// 입력값: 없음
// 출력값: 일정 등록 완료 팝업 HTML 문자열
// 기능: 선택한 분석 후보가 캘린더에 등록됐음을 알리고 캘린더로 이동시킨다.
function renderRegistrationComplete() {
  if (!state.registrationComplete) return "";
  return `<div class="sheet-scrim"></div><section class="complete-dialog" role="dialog" aria-modal="true"><span>${icon("check")}</span><h2>일정 등록이 완료됐어요</h2><p>캘린더에서 등록한 일정을 확인할 수 있어요.</p><button class="primary-button" type="button" data-action="finish-registration">캘린더 확인</button></section>`;
}

// 입력값: 없음
// 출력값: 저장 완료 화면 HTML 문자열
// 기능: 이미지에서 선택한 일정이 캘린더에 저장된 결과와 다음 행동을 보여 준다.
function renderSaveComplete() {
  const savedCandidates = state.extractionCandidates.filter((candidate) => candidate.selected);
  return `<section class="screen save-complete-screen"><div class="save-complete-content"><span class="save-check">${icon("check")}</span><h1>일정을 저장했어요</h1><p>${savedCandidates.length}개의 일정이 캘린더에 추가되었어요.</p><div class="saved-list">${savedCandidates.map((candidate) => `<div><strong>${Number(candidate.date.slice(5, 7))}월 ${Number(candidate.date.slice(8, 10))}일</strong><span>${escapeAttribute(candidate.title)}</span></div>`).join("")}</div><button class="primary-button" type="button" data-action="finish-registration">캘린더에서 확인하기</button><button class="secondary-button" type="button" data-action="reset-and-upload">다른 이미지 등록</button></div></section>`;
}

// 입력값: 없음
// 출력값: 이미지 분석 결과 확인 화면 HTML 문자열
// 기능: 여러 일정 후보를 선택·수정한 뒤 한 번에 캘린더에 등록한다.
function renderExtractionResults() {
  const selectedCount = state.extractionCandidates.filter((candidate) => candidate.selected).length;
  return `<section class="screen extraction-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="upload">${icon("back")}<span>이미지</span></button><h1>분석 결과 확인</h1><span></span></header><div class="result-scroll"><div class="result-summary"><strong>${state.extractionCandidates.length}개의 일정을<br />찾았어요</strong><p>저장할 일정을 확인하고, 필요하면 수정해 주세요.</p></div><button class="source-preview" type="button"><span>${icon("note")}<strong>원본 보기</strong></span><small>${escapeAttribute(state.upload.fileName)}</small>${icon("chevron")}</button><div class="candidate-list">${state.extractionCandidates.map(renderCandidateCard).join("")}</div><div class="result-actions"><p>선택한 일정 <strong>${selectedCount}개</strong></p><button class="primary-button" type="button" data-action="register-candidates" ${selectedCount ? "" : "disabled"}>선택한 일정 저장하기</button></div></div>${renderCandidateEditor()}</section>`;
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
  return `<section class="screen my-screen"><header class="feature-header"><h1>마이</h1><span></span></header><div class="my-scroll"><section class="profile-card"><span class="profile-avatar">${escapeAttribute(userName.slice(0, 1))}</span><div><h2>${escapeAttribute(userName)}</h2><p>${escapeAttribute(userEmail)}</p><small>고등학생 · 2학년</small></div></section><section class="my-menu-card">${renderMyMenuRow("users", "내 그룹", `${state.groups.length}개 그룹에 참여 중`, "open-groups")}<label class="notification-row"><span class="my-menu-icon">${icon("bell")}</span><span><strong>일정 알림</strong><small>마감과 시작 전에 알려드려요</small></span><input type="checkbox" data-action="toggle-notification" ${state.notificationEnabled ? "checked" : ""} /></label>${renderMyMenuRow("note", "이용약관 및 개인정보", "PLANIT 서비스 정책 확인", "open-terms-info")}</section><button class="logout-button" type="button" data-action="logout">${icon("logout")} 로그아웃</button><p class="app-version">PLANIT v1.0.0</p></div>${renderBottomNav("my")}</section>`;
}

// 입력값: 메뉴 제목, 현재 값, 이동할 화면 또는 실행 이름
// 출력값: 설정 화면의 공통 메뉴 행 HTML 문자열
// 기능: 설정 항목을 같은 정렬과 터치 영역으로 표시한다.
function renderSettingsRow(title, value, action, view = "") {
  const viewAttribute = view ? ` data-view="${view}"` : "";
  return `<button class="settings-row" type="button" data-action="${action}"${viewAttribute}><span>${title}</span><em>${value || ""}</em>${icon("chevron")}</button>`;
}

// 입력값: 없음
// 출력값: 앱 설정 화면 HTML 문자열
// 기능: 프로필·캘린더·시간표·알림·보안·앱 정보를 한곳에서 관리하게 한다.
function renderSettings() {
  const userName = state.signup.name.trim() || "지민";
  const userEmail = state.signup.email.trim() || state.login.email.trim() || "jimin@example.com";
  return `<section class="screen settings-main-screen"><div class="settings-main-scroll"><h1>설정</h1><section class="settings-profile-card"><span>${escapeAttribute(userName.slice(0, 1))}</span><div><strong>${escapeAttribute(userName)}</strong><small>${escapeAttribute(state.signup.school || "한빛고등학교")} · ${escapeAttribute(state.signup.grade || "2학년")}</small><small>${escapeAttribute(userEmail)}</small></div><button type="button" data-action="navigate" data-view="profileEdit">${icon("chevron")}</button><button class="profile-edit-link" type="button" data-action="navigate" data-view="profileEdit">프로필 편집</button></section><h2>일정 및 캘린더</h2><section class="settings-card">${renderSettingsRow("기본 캘린더", "개인", "noop")}${renderSettingsRow("기본 알림", "10분 전", "noop")}${renderSettingsRow("시간대", "Asia/Seoul", "noop")}${renderSettingsRow("주 시작 요일", "일요일", "noop")}</section><h2>시간표</h2><section class="settings-card">${renderSettingsRow("교시 시간 설정", "", "navigate", "timetableSettings")}${renderSettingsRow("과목 및 색상 관리", "", "navigate", "timetableSettings")}</section><h2>알림</h2><section class="settings-card toggle-settings-card"><label><span>일정 알림</span><input type="checkbox" data-action="toggle-notification" ${state.notificationEnabled ? "checked" : ""} /></label><label><span>그룹 일정 변경 알림</span><input type="checkbox" data-action="toggle-group-notification" ${state.groupNotificationEnabled ? "checked" : ""} /></label><label><span>마감 알림</span><input type="checkbox" data-action="toggle-deadline-notification" ${state.deadlineNotificationEnabled ? "checked" : ""} /></label></section><h2>계정 및 보안</h2><section class="settings-card">${renderSettingsRow("비밀번호 변경", "", "noop")}${renderSettingsRow("로그인 기기 관리", "", "noop")}${renderSettingsRow("개인정보 처리방침", "", "open-terms-info")}${renderSettingsRow("이용약관", "", "open-terms-info")}</section><h2>앱 정보</h2><section class="settings-card">${renderSettingsRow("버전", "1.0.0", "noop")}${renderSettingsRow("문의하기", "", "noop")}${renderSettingsRow("오픈소스 라이선스", "", "noop")}</section><button class="logout-button" type="button" data-action="logout">로그아웃</button><p class="app-version">© 2026 PLANIT</p></div>${renderBottomNav("settings")}</section>`;
}

// 입력값: 없음
// 출력값: 프로필 편집 화면 HTML 문자열
// 기능: 이름·이메일·학교·학년과 관심 일정 유형을 수정하게 한다.
function renderProfileEdit() {
  return `<section class="screen profile-edit-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="settings">${icon("back")}<span>설정</span></button><h1>프로필 편집</h1><button class="header-text-button is-strong" type="submit" form="profile-form">저장</button></header><form id="profile-form" class="profile-edit-form" data-form="profile"><span class="large-avatar">${escapeAttribute((state.signup.name || "지민").slice(0, 1))}</span><button class="photo-change-button" type="button">사진 변경</button><section class="form-section-card"><label><span>이름</span><input name="name" value="${escapeAttribute(state.signup.name || "지민")}" /></label><label><span>이메일</span><input name="email" type="email" value="${escapeAttribute(state.signup.email || "jimin@example.com")}" /></label><label><span>학교</span><input name="school" value="${escapeAttribute(state.signup.school || "한빛고등학교")}" /></label><label><span>학년</span><select name="grade">${["1학년", "2학년", "3학년"].map((grade) => `<option ${state.signup.grade === grade ? "selected" : ""}>${grade}</option>`).join("")}</select></label></section><fieldset><legend>관심 일정 유형</legend><div class="interest-chips"><button class="is-selected" type="button">${icon("check")} 시험</button><button class="is-selected" type="button">${icon("check")} 수행평가</button><button type="button">과제</button><button class="is-selected" type="button">${icon("check")} 동아리</button></div></fieldset><section class="settings-card">${renderSettingsRow("비밀번호 변경", "", "noop")}<button class="delete-account-button" type="button">회원 탈퇴</button></section><button class="primary-button" type="submit">변경사항 저장</button></form></section>`;
}

// 입력값: 없음
// 출력값: 알림 목록 화면 HTML 문자열
// 기능: 일정·그룹 알림을 기간별로 보여 주고 유형 필터와 모두 읽음을 제공한다.
function renderNotifications() {
  const notifications = [
    { type: "event", section: "오늘", title: "JAVA 수행평가가 10분 뒤 시작해요.", meta: "일정 · 08:50" },
    { type: "group", section: "오늘", title: "동아리 멘토링 일정이 변경되었어요.", meta: "그룹 · 오전 11:20" },
    { type: "event", section: "오늘", title: "과학 보고서 제출 마감이 오늘이에요.", meta: "일정 · 오전 8:00" },
    { type: "event", section: "이번 주", title: "수학 시험이 3일 남았어요.", meta: "일정 · 월요일" },
    { type: "group", section: "이번 주", title: "학급 사진 촬영 일정이 추가되었어요.", meta: "그룹 · 월요일" },
    { type: "group", section: "이전 알림", title: "프로젝트 중간 점검 일정이 추가되었어요.", meta: "그룹 · 11월 9일" },
  ];
  const filtered = state.notificationFilter === "all" ? notifications : notifications.filter((item) => item.type === state.notificationFilter);
  const sections = ["오늘", "이번 주", "이전 알림"];
  return `<section class="screen notifications-screen"><div class="notifications-scroll"><header><button type="button" data-action="navigate" data-view="home">${icon("back")}</button><button type="button" data-action="mark-all-read">모두 읽음</button></header><h1>알림</h1><div class="segment-control notification-segments"><button class="${state.notificationFilter === "all" ? "is-active" : ""}" type="button" data-action="set-notification-filter" data-filter="all">전체</button><button class="${state.notificationFilter === "event" ? "is-active" : ""}" type="button" data-action="set-notification-filter" data-filter="event">일정</button><button class="${state.notificationFilter === "group" ? "is-active" : ""}" type="button" data-action="set-notification-filter" data-filter="group">그룹</button></div>${sections.map((section) => { const items = filtered.filter((item) => item.section === section); return items.length ? `<section class="notification-section"><h2>${section}</h2><div class="notification-card">${items.map((item) => `<button type="button"><i></i><span><strong>${item.title}</strong><small>${icon(item.type === "group" ? "users" : "bell")}${item.meta}</small></span></button>`).join("")}</div></section>` : ""; }).join("")}</div>${renderBottomNav("")}</section>`;
}

// 입력값: 없음
// 출력값: 일정 검색 화면 HTML 문자열
// 기능: 제목과 장소를 기준으로 현재 프론트엔드 일정 목록을 빠르게 탐색하게 한다.
function renderSearch() {
  return `<section class="screen search-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="home">${icon("back")}<span>홈</span></button><h1>일정 검색</h1><span></span></header><div class="search-content"><label class="search-field">${icon("search")}<input type="search" placeholder="일정 제목이나 장소를 검색해 보세요" /></label><h2>다가오는 일정</h2><div class="calendar-event-list">${renderCalendarEvents(state.events.filter((eventItem) => eventItem.date >= "2026-11-17").slice(0, 6))}</div></div></section>`;
}

// 입력값: 없음
// 출력값: 서비스 약관 안내 화면 HTML 문자열
// 기능: 가입 시 동의한 서비스 이용 및 개인정보 안내를 다시 확인하게 한다.
function renderTermsInfo() {
  return `<section class="screen terms-info-screen"><header class="feature-header centered"><button type="button" data-action="navigate" data-view="settings" aria-label="설정으로 돌아가기">${icon("back")}</button><h1>이용약관 및 개인정보</h1><span></span></header><div class="terms-info-scroll"><section><h2>서비스 이용약관</h2><p>PLANIT은 안내문에서 추출한 일정 정보를 사용자가 확인하고 관리할 수 있도록 돕는 서비스입니다. 분석 결과는 자동 확정되지 않으며 등록 전에 사용자가 확인해야 합니다.</p></section><section><h2>개인정보 수집·이용 안내</h2><p>계정과 일정 제공에 필요한 최소 정보만 사용합니다. 이미지 원문과 민감 정보는 불필요하게 노출하거나 기록하지 않습니다.</p></section><section><h2>일정 알림 안내</h2><p>선택한 알림 시간에 따라 일정 시작 또는 마감 전에 안내합니다. 설정 화면에서 언제든 알림을 끌 수 있습니다.</p></section></div></section>`;
}

// 입력값: 그룹 객체
// 출력값: 그룹 목록 카드 HTML 문자열
// 기능: 참여 중인 그룹의 이름, 설명, 인원과 공유 일정 수를 보여 준다.
function renderGroupCard(group) {
  return `<button class="group-card" type="button" data-action="open-group" data-group-id="${group.id}"><span class="group-symbol color-${group.color}">${escapeAttribute(group.name.slice(0, 1))}</span><span class="group-card-main"><strong>${escapeAttribute(group.name)}</strong><small>${escapeAttribute(group.description)}</small><em>${icon("users")} ${group.members.length}명 · 공유 일정 ${group.eventIds.length}개</em></span>${icon("chevron")}</button>`;
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
  return `<section class="screen groups-screen"><div class="groups-scroll"><header class="groups-header"><h1>그룹</h1><p>함께 관리하는 일정</p></header><div class="group-action-row"><button class="primary-button" type="button" data-action="open-group-create">${icon("plus")} 그룹 만들기</button><button type="button" data-action="open-group-join">${icon("users")} 그룹 참여</button></div><div class="groups-heading"><h2>내 그룹 <em>${state.groups.length}</em></h2></div><div class="group-list">${state.groups.map(renderGroupCard).join("")}</div><p class="groups-footnote">내 개인 일정은 직접 공유를 선택한 경우에만 그룹에 보여요.</p></div>${renderBottomNav("groups")}</section>`;
}

// 입력값: 없음
// 출력값: 그룹 만들기 화면 HTML 문자열
// 기능: 그룹 이름·유형·설명·대표 아이콘·기본 알림을 입력받는다.
function renderGroupCreate() {
  return `<section class="screen group-form-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="groups">${icon("back")}<span>그룹</span></button><h1>그룹 만들기</h1><span></span></header><form class="standalone-group-form" data-form="group"><h2>새 그룹을<br />만들어요</h2><label class="board-input-card"><span>그룹 이름</span><input name="name" value="${escapeAttribute(state.groupDraft.name)}" placeholder="예: 코딩 동아리" /></label><fieldset><legend>그룹 유형</legend><div class="group-type-chips">${["반", "동아리", "스터디", "조별 과제", "기타"].map((type) => `<button class="${state.groupDraft.type === type ? "is-selected" : ""}" type="button" data-action="select-group-type" data-type="${type}">${state.groupDraft.type === type ? icon("check") : ""}${type}</button>`).join("")}</div></fieldset><label class="board-input-card description-input"><span>그룹 설명</span><textarea name="description" placeholder="그룹을 소개하는 글을 남겨 보세요">${escapeAttribute(state.groupDraft.description)}</textarea></label><fieldset><legend>그룹 대표 아이콘</legend><div class="group-icon-picker">${["users", "calendar", "lock", "note", "timetable", "bell"].map((iconName) => `<button class="${state.groupDraft.icon === iconName ? "is-selected" : ""}" type="button" data-action="select-group-icon" data-icon="${iconName}">${icon(iconName)}</button>`).join("")}</div></fieldset><label class="inline-card-row"><span>그룹 일정 기본 알림</span><select name="alarm"><option>10분 전</option><option>30분 전</option><option>1시간 전</option></select></label><p class="form-help">만들고 나면 내가 이 그룹의 소유자가 돼요.</p><button class="primary-button" type="submit" ${state.groupDraft.name.trim() ? "" : "disabled"}>만들기</button></form></section>`;
}

// 입력값: 없음
// 출력값: 초대 코드 그룹 참여 화면 HTML 문자열
// 기능: 초대 코드를 입력해 그룹 정보를 확인하고 참여하게 한다.
function renderGroupJoin() {
  const previewGroup = state.groups.find((group) => group.code.toLowerCase() === state.groupDraft.code.trim().toLowerCase());
  return `<section class="screen group-form-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="groups">${icon("back")}<span>그룹</span></button><h1>그룹 참여</h1><span></span></header><form class="standalone-group-form join-group-form" data-form="group"><h2>그룹에<br />참여해요</h2><p>받은 초대 코드나 링크를 입력해 주세요.</p><label class="board-input-card"><span>초대 코드 또는 링크</span><input name="code" value="${escapeAttribute(state.groupDraft.code)}" placeholder="PL-7K2M-90" /></label>${previewGroup ? `<section class="join-preview"><span class="group-symbol color-${previewGroup.color}">${escapeAttribute(previewGroup.name.slice(0, 1))}</span><h3>${escapeAttribute(previewGroup.name)}</h3><small>${icon("users")} 구성원 ${previewGroup.members.length}명</small><p>${escapeAttribute(previewGroup.description)}</p></section>` : ""}${state.groupError ? `<p class="group-error">${state.groupError}</p>` : ""}<button class="primary-button" type="submit" ${state.groupDraft.code.trim() ? "" : "disabled"}>그룹 참여하기</button></form></section>`;
}

// 입력값: 그룹에서 공유 중인 일정 ID 목록
// 출력값: 공유 일정 카드 HTML 문자열
// 기능: 그룹 상세 화면에서 구성원이 함께 보는 일정을 보여 준다.
function renderGroupEvents(eventIds) {
  const events = state.events.filter((item) => eventIds.includes(item.id));
  if (!events.length) return `<p class="group-empty">아직 공유된 일정이 없어요.</p>`;
  return events.map((item) => `<button class="group-event-row" type="button" data-action="open-event" data-event-id="${item.id}"><span>${item.date.slice(5).replace("-", "/")}</span><div><strong>${escapeAttribute(item.title)}</strong><small>${item.start} · ${escapeAttribute(item.location || "장소 없음")}</small></div>${icon("chevron")}</button>`).join("");
}

// 입력값: 없음
// 출력값: 선택한 그룹 상세 화면 HTML 문자열
// 기능: 그룹 구성원, 초대 코드와 공유 일정을 확인하고 새 그룹 일정을 등록한다.
function renderGroupDetail() {
  const group = state.groups.find((item) => item.id === state.selectedGroupId) ?? state.groups[0];
  const eventsContent = `<section class="group-section"><div class="group-event-list">${renderGroupEvents(group.eventIds)}</div></section>`;
  const membersContent = `<section class="group-section member-list-section"><div class="group-section-heading"><h3>구성원</h3><span>${group.members.length}명</span></div>${group.members.map((member, index) => `<div class="member-row"><span>${escapeAttribute(member.slice(0, 1))}</span><strong>${escapeAttribute(member)}</strong>${index === 0 ? `<em>소유자</em>` : ""}</div>`).join("")}</section>`;
  const infoContent = `<section class="group-section group-info-section"><p>${escapeAttribute(group.description)}</p><div class="invite-code-card"><div><small>초대 코드</small><strong>${escapeAttribute(group.code)}</strong></div><button type="button" data-action="copy-group-code" data-code="${escapeAttribute(group.code)}">${icon("link")} 복사</button></div><button class="leave-group-button" type="button" data-action="leave-group">그룹 나가기</button></section>`;
  const tabContent = state.groupDetailTab === "members" ? membersContent : state.groupDetailTab === "info" ? infoContent : eventsContent;
  return `<section class="screen group-detail-screen"><header class="board-page-header"><button type="button" data-action="navigate" data-view="groups">${icon("back")}<span>그룹</span></button><span></span><button class="round-more" type="button" aria-label="그룹 메뉴">${icon("more")}</button></header><div class="group-detail-scroll"><section class="group-detail-hero"><span class="group-symbol color-${group.color}">${escapeAttribute(group.name.slice(0, 1))}</span><div><h1>${escapeAttribute(group.name)}</h1><p>${icon("users")} 구성원 ${group.members.length}명 · 공유 일정 ${group.eventIds.length}개</p></div></section><label class="group-notification-toggle">${icon("bell")}<span>그룹 일정 알림</span><input type="checkbox" data-action="toggle-group-notification" ${state.groupNotificationEnabled ? "checked" : ""} /></label><div class="segment-control group-detail-tabs"><button class="${state.groupDetailTab === "events" ? "is-active" : ""}" type="button" data-action="set-group-detail-tab" data-tab="events">공유 일정</button><button class="${state.groupDetailTab === "members" ? "is-active" : ""}" type="button" data-action="set-group-detail-tab" data-tab="members">구성원</button><button class="${state.groupDetailTab === "info" ? "is-active" : ""}" type="button" data-action="set-group-detail-tab" data-tab="info">그룹 정보</button></div>${tabContent}<button class="primary-button group-add-event" type="button" data-action="new-group-event">그룹 일정 추가</button></div></section>`;
}

// 입력값: 없음
// 출력값: 그룹 공유 선택 팝업 HTML 문자열
// 기능: 현재 일정을 공유할 그룹을 복수 선택하고 적용한다.
function renderShareGroupDialog() {
  if (!state.groupShareOpen) return "";
  return `<div class="sheet-scrim" data-action="close-share-groups"></div><section class="bottom-sheet share-groups-sheet" role="dialog" aria-modal="true"><div class="sheet-handle"></div><div class="sheet-title-row"><h2>그룹에 공유</h2><button type="button" data-action="close-share-groups">${icon("close")}</button></div><p class="share-help">이 일정을 함께 볼 그룹을 선택해 주세요.</p><div class="share-group-list">${state.groups.map((group) => { const selected = state.selectedShareGroupIds.includes(group.id); return `<button class="${selected ? "is-selected" : ""}" type="button" data-action="toggle-share-group" data-group-id="${group.id}"><span class="group-symbol color-${group.color}">${escapeAttribute(group.name.slice(0, 1))}</span><span><strong>${escapeAttribute(group.name)}</strong><small>${group.members.length}명</small></span><i>${selected ? icon("check") : ""}</i></button>`; }).join("")}</div><button class="primary-button" type="button" data-action="confirm-share-groups" ${state.selectedShareGroupIds.length ? "" : "disabled"}>선택 그룹에 공유</button></section>`;
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
    cameraPermission: renderCameraPermission,
    camera: renderCamera,
    cameraReview: renderCameraReview,
    extractionResults: renderExtractionResults,
    saveComplete: renderSaveComplete,
    my: renderMy,
    settings: renderSettings,
    profileEdit: renderProfileEdit,
    notifications: renderNotifications,
    search: renderSearch,
    termsInfo: renderTermsInfo,
    groups: renderGroups,
    groupCreate: renderGroupCreate,
    groupJoin: renderGroupJoin,
    groupDetail: renderGroupDetail,
  };
  persistTemporaryData();
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

  if (input.dataset.timeField && input.dataset.timeKind) {
    if (!state.timetableTimeDraft) prepareTimetableTimeDraft();
    if (input.dataset.timeKind === "lunch") {
      state.timetableTimeDraft.lunch[input.dataset.timeField] = input.value;
    } else if (input.dataset.timeKind === "routine") {
      state.timetableTimeDraft.routines[input.dataset.routine][input.dataset.timeField] = input.value;
    } else {
      const period = state.timetableTimeDraft.periods.find((entry) => entry.period === Number(input.dataset.period));
      if (period) period[input.dataset.timeField] = input.value;
    }
    state.timetableTimeError = "";
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

  if (form.dataset.form === "profile") {
    state.signup[input.name] = input.value;
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
    const requiredValue = state.groupModalTab === "create" ? state.groupDraft.name : state.groupDraft.code;
    if (submitButton) submitButton.disabled = !requiredValue.trim();
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

  if (form.dataset.form === "profile") {
    state.currentView = "settings";
    renderApp();
  }


  if (form.dataset.form === "class" && state.classDraft.title.trim()) {
    const subjectTitle = state.classDraft.title.trim();
    const normalizedTitle = subjectTitle.replace(/\s+/g, " ").toLocaleLowerCase("ko-KR");
    const matchingSubject = state.timetable.find((item) => item.id !== state.editingClassId && item.title.trim().replace(/\s+/g, " ").toLocaleLowerCase("ko-KR") === normalizedTitle);
    const subjectColor = !state.classColorChanged && matchingSubject ? matchingSubject.color : state.classDraft.color;
    const classData = { ...state.classDraft, title: subjectTitle, color: subjectColor, period: Number(state.classDraft.period) };
    if (state.editingClassId) {
      const classItem = state.timetable.find((item) => item.id === state.editingClassId);
      Object.assign(classItem, classData);
    } else {
      state.timetable.push({ id: Date.now(), ...classData });
    }
    if (state.classColorChanged) applySubjectColor(subjectTitle, subjectColor);
    state.selectedWeekday = state.classDraft.day;
    state.classEditorOpen = false;
    renderApp();
  }


  if (form.dataset.form === "event" && state.eventDraft.title.trim()) {
    if (!state.eventDraft.allDay && state.eventDraft.end <= state.eventDraft.start) {
      state.eventFormError = "종료 시간은 시작 시간보다 늦어야 해요.";
      renderApp();
      return;
    }
    state.eventFormError = "";
    if (state.eventFormMode === "edit") {
      const eventItem = state.events.find((item) => item.id === state.selectedEventId);
      Object.assign(eventItem, state.eventDraft);
    } else {
      const isGroupEvent = state.eventReturnView === "groupDetail" || state.eventDraft.category === "그룹" || state.eventDraft.sharedGroup !== "공유 안 함";
      const newEvent = { id: Date.now(), ...state.eventDraft, shared: isGroupEvent, temporary: true };
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
    if (state.eventReturnView !== "eventDetail") state.eventDetailReturnView = state.eventReturnView;
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
      const group = { id: Date.now(), name: state.groupDraft.name.trim(), description: state.groupDraft.description.trim() || `${state.groupDraft.type} 일정 공유 그룹`, code: `PL-${String(Date.now()).slice(-6)}`, color: "pink", members: [state.signup.name.trim() || "지민"], eventIds: [] };
      state.groups.push(group);
      state.selectedGroupId = group.id;
      state.groupModalOpen = false;
      state.groupDraft = { name: "", code: "", description: "", type: "동아리", icon: "users" };
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
        state.groupDraft = { name: "", code: "", description: "", type: "동아리", icon: "users" };
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
  if (action === "select-grade") state.signup.grade = target.dataset.grade;

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

  if (action === "open-terms-detail") state.termsDetail = target.dataset.term;
  if (action === "close-terms-detail") state.termsDetail = null;
  if (action === "agree-detail-term") {
    state.terms[target.dataset.term] = true;
    state.termsDetail = null;
  }

  if (action === "complete-signup" && state.terms.service && state.terms.privacy) {
    state.currentView = "home";
  }

  if (action === "set-filter") state.homeFilter = target.dataset.filter;
  if (action === "set-calendar-filter") state.calendarFilter = target.dataset.filter;
  if (action === "set-notification-filter") state.notificationFilter = target.dataset.filter;
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
  if (action === "toggle-search") state.currentView = "search";
  if (action === "select-weekday") state.selectedWeekday = target.dataset.day;
  if (action === "open-timetable-settings") {
    prepareTimetableTimeDraft();
    state.currentView = "timetableSettings";
  }
  if (action === "set-timetable-tab") state.timetableSettingsTab = target.dataset.tab;
  if (action === "apply-timetable-times" && state.timetableTimeDraft) {
    if (!isTimetableTimeDraftValid(state.timetableTimeDraft)) {
      state.timetableTimeError = "종료 시간은 시작 시간보다 늦어야 해요.";
    } else {
      state.periodTimes = state.timetableTimeDraft.periods.map((period) => ({ ...period }));
      state.lunchTime = { ...state.timetableTimeDraft.lunch };
      state.routineTimes = Object.fromEntries(Object.entries(state.timetableTimeDraft.routines).map(([key, routine]) => [key, { ...routine }]));
      state.timetableTimeError = "";
      state.currentView = "timetable";
    }
  }
  if (action === "add-class") openClassEditor();
  if (action === "add-class-for-slot") {
    state.selectedWeekday = target.dataset.day;
    openClassEditor();
    state.classDraft.period = target.dataset.period;
  }
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
  if (action === "choose-class-color") {
    state.classDraft.color = target.dataset.color;
    state.classColorChanged = true;
  }

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
    state.eventDetailReturnView = state.currentView === "groupDetail" ? "groupDetail" : state.currentView === "home" ? "home" : "calendar";
    state.selectedEventId = Number(target.dataset.eventId);
    state.currentView = "eventDetail";
  }
  if (action === "new-event") {
    state.addMenuOpen = false;
    const returnView = ["home", "calendar", "timetable", "groups", "settings"].includes(state.currentView) ? state.currentView : "calendar";
    openEventForm(null, returnView);
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
    state.groups.forEach((group) => {
      group.eventIds = group.eventIds.filter((id) => id !== state.selectedEventId);
    });
    state.selectedEventId = null;
    state.eventDeleteOpen = false;
    state.currentView = state.eventDetailReturnView;
  }
  if (action === "select-event-category") {
    state.eventDraft.category = target.dataset.category;
    if (state.eventDraft.category === "개인") {
      state.eventDraft.sharedGroup = "공유 안 함";
    } else if (state.eventDraft.sharedGroup === "공유 안 함") {
      const selectedGroup = state.groups.find((group) => group.id === state.selectedGroupId) ?? state.groups[0];
      state.eventDraft.sharedGroup = selectedGroup?.name ?? "공유 안 함";
    }
  }
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
    state.currentView = "upload";
  }
  if (action === "open-camera") state.currentView = state.cameraPermissionSeen ? "camera" : "cameraPermission";
  if (action === "allow-camera") {
    state.cameraPermissionSeen = true;
    state.currentView = "camera";
  }
  if (action === "retake-photo") {
    state.currentView = "camera";
    state.upload.status = "idle";
  }
  if (action === "use-camera-photo" && state.upload.previewUrl) {
    state.currentView = "upload";
    state.upload.status = "analyzing";
    renderApp();
    window.setTimeout(() => {
      state.extractionCandidates = createExtractionCandidates();
      state.currentView = "extractionResults";
      renderApp();
    }, 850);
    return;
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
      state.events.push({ ...candidate, id: Date.now() + index, shared: false, temporary: true });
    });
    const firstRegisteredDate = selectedCandidates[0]?.date;
    if (firstRegisteredDate) {
      const [year, month, day] = firstRegisteredDate.split("-").map(Number);
      state.calendarYear = year;
      state.calendarMonth = month;
      state.calendarSelectedDay = day;
    }
    state.registrationComplete = false;
    state.currentView = "saveComplete";
  }
  if (action === "finish-registration") {
    state.registrationComplete = false;
    state.currentView = "calendar";
  }
  if (action === "reset-and-upload") {
    if (state.upload.previewUrl) URL.revokeObjectURL(state.upload.previewUrl);
    state.upload = { status: "idle", fileName: "", fileSize: 0, previewUrl: "", error: "" };
    state.extractionCandidates = [];
    state.currentView = "upload";
  }
  if (action === "open-groups") state.currentView = "groups";
  if (action === "open-terms-info") state.currentView = "termsInfo";
  if (action === "toggle-notification") state.notificationEnabled = !state.notificationEnabled;
  if (action === "toggle-group-notification") state.groupNotificationEnabled = !state.groupNotificationEnabled;
  if (action === "toggle-deadline-notification") state.deadlineNotificationEnabled = !state.deadlineNotificationEnabled;
  if (action === "mark-all-read") state.notificationFilter = state.notificationFilter;
  if (action === "logout") {
    state.currentView = "login";
    state.login.password = "";
    window.sessionStorage.removeItem(temporaryStorageKey);
  }
  if (action === "open-group-modal") {
    state.groupModalOpen = true;
    state.groupModalTab = "create";
    state.groupError = "";
  }
  if (action === "open-group-create") {
    state.groupModalTab = "create";
    state.groupError = "";
    state.currentView = "groupCreate";
  }
  if (action === "open-group-join") {
    state.groupModalTab = "join";
    state.groupError = "";
    state.currentView = "groupJoin";
  }
  if (action === "close-group-modal") state.groupModalOpen = false;
  if (action === "set-group-tab") {
    state.groupModalTab = target.dataset.tab;
    state.groupError = "";
  }
  if (action === "select-group-type") state.groupDraft.type = target.dataset.type;
  if (action === "select-group-icon") state.groupDraft.icon = target.dataset.icon;
  if (action === "set-group-detail-tab") state.groupDetailTab = target.dataset.tab;
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
    const selectedGroup = state.groups.find((group) => group.id === state.selectedGroupId);
    state.eventDraft.sharedGroup = selectedGroup?.name ?? "공유 안 함";
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

// 입력값: 사용자가 선택하거나 드래그한 이미지 파일, 선택 경로 이름
// 출력값: 없음
// 기능: 이미지 형식과 용량을 검증하고 안전한 로컬 미리보기 주소를 만든다.
function processImageFile(file, source = "gallery") {
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
  state.currentView = source === "camera" ? "cameraReview" : "upload";
  renderApp();
}

// 입력값: 파일 선택 변경 이벤트
// 출력값: 없음
// 기능: 파일 선택창에서 고른 첫 이미지를 공통 검증 함수로 전달한다.
function handleFileChange(event) {
  const input = event.target.closest('input[type="file"]');
  if (!input) return;
  const [file] = input.files;
  const source = ["camera-image", "camera-gallery"].includes(input.id) ? "camera" : "gallery";
  if (file) processImageFile(file, source);
}

// 입력값: 파일 드래그 이벤트
// 출력값: 없음
// 기능: 업로드 영역의 브라우저 기본 동작을 막고 드래그 상태를 강조한다.
function handleDragOver(event) {
  const dropzone = event.target.closest(".upload-dropzone");
  if (!dropzone) return;
  event.preventDefault();
  dropzone.classList.add("is-dragging");
}

// 입력값: 파일 드래그 종료 이벤트
// 출력값: 없음
// 기능: 파일이 업로드 영역을 벗어나면 강조 상태를 해제한다.
function handleDragLeave(event) {
  const dropzone = event.target.closest(".upload-dropzone");
  if (dropzone) dropzone.classList.remove("is-dragging");
}

// 입력값: 파일 드롭 이벤트
// 출력값: 없음
// 기능: 업로드 영역에 놓은 첫 이미지를 공통 검증 함수로 전달한다.
function handleDrop(event) {
  const dropzone = event.target.closest(".upload-dropzone");
  if (!dropzone) return;
  event.preventDefault();
  dropzone.classList.remove("is-dragging");
  const [file] = event.dataTransfer.files;
  if (file) processImageFile(file);
}

app.addEventListener("input", handleInput);
app.addEventListener("change", handleFileChange);
app.addEventListener("dragover", handleDragOver);
app.addEventListener("dragleave", handleDragLeave);
app.addEventListener("drop", handleDrop);
app.addEventListener("submit", handleSubmit);
app.addEventListener("click", handleClick);
restoreTemporaryData();
renderApp();
