const app = document.querySelector("#app");
const bottomNav = document.querySelector(".bottom-nav");

const state = {
  currentView: "home",
  selectedDate: "23",
  selectedWeekday: "금",
  timetableHasClasses: false,
  uploadImageUrl: "",
  uploadError: "",
  analysisStatus: "idle",
  candidateDateConfirmed: false,
};

const icons = {
  home: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m4 10 8-6 8 6v9a1 1 0 0 1-1 1h-5v-6H10v6H5a1 1 0 0 1-1-1v-9Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  timetable: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M9 4v16M4 9h16M4 14h16" fill="none" stroke="currentColor" stroke-width="1.4"/></svg>`,
  calendar: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="5" width="16" height="15" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M8 3v4M16 3v4M4 9h16" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  user: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="8" r="3.2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M5 20c.7-3.2 3.1-5 7-5s6.3 1.8 7 5" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  bell: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M6 17h12l-1.2-1.8V10a4.8 4.8 0 0 0-9.6 0v5.2L6 17Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/><path d="M10 20h4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  image: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="4" y="4" width="16" height="16" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="9" cy="9" r="1.5" fill="none" stroke="currentColor" stroke-width="1.5"/><path d="m5 17 4.5-4 3 2.5 2-2 4.5 4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  camera: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M5 8h3l1.5-2h5L16 8h3a1 1 0 0 1 1 1v9a1 1 0 0 1-1 1H5a1 1 0 0 1-1-1V9a1 1 0 0 1 1-1Z" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="13" r="3" fill="none" stroke="currentColor" stroke-width="1.8"/></svg>`,
  plus: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M12 5v14M5 12h14" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round"/></svg>`,
  back: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5-7 7 7 7" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  edit: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m14 5 5 5M5 19l3.5-.8L18 8.7a2.1 2.1 0 0 0-3-3L5.5 15.2 5 19Z" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linejoin="round"/></svg>`,
  chevron: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m9 5 7 7-7 7" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  check: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="m5 12 4 4L19 6" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round"/></svg>`,
  clock: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="12" cy="12" r="8" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M12 7v5l3 2" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
  location: `<svg viewBox="0 0 24 24" aria-hidden="true"><path d="M19 10c0 5-7 10-7 10S5 15 5 10a7 7 0 1 1 14 0Z" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="12" cy="10" r="2" fill="none" stroke="currentColor" stroke-width="1.6"/></svg>`,
  calendarSmall: `<svg viewBox="0 0 24 24" aria-hidden="true"><rect x="5" y="5" width="14" height="14" rx="2" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M8 3v4M16 3v4M5 9h14" fill="none" stroke="currentColor" stroke-width="1.8"/></svg>`,
  group: `<svg viewBox="0 0 24 24" aria-hidden="true"><circle cx="9" cy="9" r="3" fill="none" stroke="currentColor" stroke-width="1.8"/><circle cx="16" cy="10" r="2.5" fill="none" stroke="currentColor" stroke-width="1.8"/><path d="M3.5 19c.7-3 2.5-4.5 5.5-4.5s4.8 1.5 5.5 4.5M14 15c3 0 4.8 1.3 5.5 4" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round"/></svg>`,
};

// 입력값: 아이콘 이름
// 출력값: 화면에 삽입할 SVG 문자열
// 기능: Figma 화면에서 사용한 선형 아이콘을 화면 요소에 제공한다.
function icon(name) {
  return icons[name] ?? "";
}

// 입력값: 없음
// 출력값: 날짜 스트립 HTML 문자열
// 기능: 홈 화면 상단에 표시할 일주일 날짜를 렌더링한다.
function renderDateStrip() {
  const dates = [
    ["일", "18", true],
    ["월", "19", false],
    ["화", "20", false],
    ["수", "21", false],
    ["목", "22", false],
    ["금", "23", false],
    ["토", "24", false],
  ];

  return dates
    .map(([day, date, isSunday]) => `<button class="date-item ${isSunday ? "is-sunday" : ""} ${date === state.selectedDate ? "is-selected" : ""}" type="button" data-date="${date}"><span>${day}</span><span>${date}</span></button>`)
    .join("");
}

// 입력값: 없음
// 출력값: 홈 화면 HTML 문자열
// 기능: 시간표 안내, 오늘의 일정, 빠른 일정 추가 버튼을 표시한다.
function renderHome() {
  return `
    <section class="home-page">
      <header class="home-header">
        <h1>10월 23일 금요일</h1>
        <button class="icon-button" type="button" aria-label="알림">${icon("bell")}</button>
      </header>
      <div class="date-strip" aria-label="날짜 선택">${renderDateStrip()}</div>

      <section class="soft-card timetable-prompt">
        <span class="card-icon">${icon("timetable")}</span>
        <h2>시간표를 먼저 등록해 주세요</h2>
        <p>수업과 개인 일정을 한 줄로 모아 보여 드려요</p>
        <button class="primary-button" type="button" data-action="open-timetable">시간표 등록하기</button>
      </section>

      <div class="section-heading">
        <h2>오늘의 일정</h2>
      </div>
      <section class="empty-event-card">
        <span class="card-icon">${icon("calendarSmall")}</span>
        <h2>오늘은 일정이 없어요</h2>
        <p>＋ 버튼으로 공지 사진이나 직접 입력으로 추가해요</p>
      </section>
      <button class="floating-add" type="button" aria-label="일정 추가" data-action="open-upload">${icon("plus")}</button>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 시간표 화면 HTML 문자열
// 기능: 등록된 시간표가 없을 때의 빈 상태를 Figma 화면과 동일하게 표시한다.
function renderTimetable() {
  if (state.timetableHasClasses) {
    return renderTimetableFilled();
  }

  return `
    <section class="timetable-page">
      <header class="screen-header timetable-header">
        <div>
          <h1 class="screen-title">내 시간표</h1>
          <p class="screen-subtitle">2학년 3반 · 월~금</p>
        </div>
        <button class="text-button" type="button" data-action="open-timetable-settings">${icon("edit")} 편집</button>
      </header>
      <div class="weekday-tabs" aria-label="요일 선택">
        ${["월", "화", "수", "목", "금"].map((day) => `<button class="weekday-button ${day === state.selectedWeekday ? "is-selected" : ""}" type="button" data-weekday="${day}">${day}</button>`).join("")}
      </div>
      <section class="empty-timetable-card">
        <span class="card-icon">${icon("timetable")}</span>
        <h2>등록된 수업이 없어요</h2>
        <p>교시 시간을 정하고 과목을 넣어 주세요</p>
        <button class="primary-button" type="button" data-action="open-timetable-settings">시간표 만들기</button>
      </section>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 등록된 시간표 화면 HTML 문자열
// 기능: 시간표가 등록된 뒤의 기본 과목 카드 목록을 표시한다.
function renderTimetableFilled() {
  const classes = [
    ["1교시", "09:00 - 09:50", "국어", "3반 교실"],
    ["2교시", "10:00 - 10:50", "수학", "3반 교실"],
    ["3교시", "11:00 - 11:50", "영어", "3반 교실"],
    ["4교시", "12:00 - 12:50", "과학", "과학실"],
  ];

  return `
    <section class="timetable-page">
      <header class="screen-header timetable-header">
        <div>
          <h1 class="screen-title">내 시간표</h1>
          <p class="screen-subtitle">2학년 3반 · 월~금</p>
        </div>
        <button class="text-button" type="button" data-action="open-timetable-settings">편집</button>
      </header>
      <div class="weekday-tabs" aria-label="요일 선택">
        ${["월", "화", "수", "목", "금"].map((day) => `<button class="weekday-button ${day === state.selectedWeekday ? "is-selected" : ""}" type="button" data-weekday="${day}">${day}</button>`).join("")}
      </div>
      <div class="timetable-grid">
        ${classes.map(([period, time, name, location]) => `<article class="class-card"><div class="class-period">${period}<span class="class-time">${time}</span></div><div><span class="class-name">${name}</span><span class="class-location">${location}</span></div><span class="class-arrow">${icon("chevron")}</span></article>`).join("")}
      </div>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 시간표 설정 화면 HTML 문자열
// 기능: 요일별 과목을 설정하는 Figma 화면을 표시한다.
function renderTimetableSettings() {
  const classes = [
    ["1교시", "09:00 - 09:50", "국어", "3반 교실"],
    ["2교시", "10:00 - 10:50", "수학", "3반 교실"],
    ["3교시", "11:00 - 11:50", "영어", "3반 교실"],
    ["4교시", "12:00 - 12:50", "과학", "과학실"],
    ["5교시", "14:00 - 14:50", "과학", "과학실"],
    ["6교시", "15:00 - 15:50", "한국사", "3반 교실"],
  ];

  return `
    <section class="settings-page">
      <header class="screen-header">
        <button class="back-button" type="button" aria-label="뒤로" data-action="back-timetable">${icon("back")}</button>
        <h1 class="screen-title">시간표 설정</h1>
        <button class="text-button" type="button" data-action="save-timetable">저장</button>
      </header>
      <div class="settings-tabs" role="tablist" aria-label="시간표 설정 방식">
        <button class="settings-tab" type="button">교시 시간</button>
        <button class="settings-tab is-selected" type="button">요일별 과목</button>
      </div>
      <div class="weekday-tabs" aria-label="요일 선택">
        ${["월", "화", "수", "목", "금"].map((day) => `<button class="weekday-button ${day === state.selectedWeekday ? "is-selected" : ""}" type="button" data-weekday="${day}">${day}</button>`).join("")}
      </div>
      <div class="timetable-grid">
        ${classes.map(([period, time, name, location]) => `<article class="class-card"><div class="class-period">${period}<span class="class-time">${time}</span></div><div><span class="class-name">${name}</span><span class="class-location">${location}</span></div><span class="class-arrow">${icon("chevron")}</span></article>`).join("")}
      </div>
      <button class="add-class-button" type="button" data-action="add-class">＋ 수업 추가</button>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 캘린더 화면 HTML 문자열
// 기능: 월간 캘린더와 선택 날짜의 개인·공유·마감 일정을 표시한다.
function renderCalendar() {
  const days = ["일", "월", "화", "수", "목", "금", "토"];
  const dates = ["27", "28", "29", "30", "1", "2", "3", "4", "5", "6", "7", "8", "9", "10", "11", "12", "13", "14", "15", "16", "17", "18", "19", "20", "21", "22", "23", "24", "25", "26", "27", "28", "29", "30", "31"];

  return `
    <section class="calendar-page">
      <header class="calendar-header">
        <h1 class="screen-title">캘린더</h1>
        <div class="month-selector"><button type="button" aria-label="이전 달">‹</button><span>2026년 10월</span><button type="button" aria-label="다음 달">›</button></div>
      </header>
      <div class="calendar-grid">
        ${days.map((day) => `<span class="calendar-weekday">${day}</span>`).join("")}
        ${dates.map((date, index) => { const muted = index < 4; const sunday = index % 7 === 0; const selected = date === "23" && index === 26; const markers = ["6", "15", "20", "23", "27", "28", "29", "30", "31"].includes(date); return `<button class="calendar-day ${muted ? "is-muted" : ""} ${sunday ? "is-sunday" : ""} ${selected ? "is-selected" : ""}" type="button" data-calendar-date="${date}">${date}${markers ? `<span class="calendar-markers"><i class="calendar-marker"></i>${date !== "23" ? "" : "<i class=\"calendar-marker is-personal\"></i><i class=\"calendar-marker is-deadline\"></i>"}</span>` : ""}</button>`; }).join("")}
      </div>
      <div class="legend"><span class="legend-item"><i class="legend-dot"></i>공유</span><span class="legend-item"><i class="legend-dot is-personal"></i>개인</span><span class="legend-item"><i class="legend-dot is-deadline"></i>마감</span></div>
      <section class="event-list">
        <div class="event-list-header"><h2>10월 23일 금요일</h2><span class="event-count">일정 3</span></div>
        <article class="event-card"><span class="event-time">종일</span><div><div class="event-title">영어 수행평가 제출</div><div class="event-meta">마감 · 23:59까지</div></div><span class="event-badge is-deadline">마감</span></article>
        <article class="event-card"><span class="event-time">14:00</span><div><div class="event-title">과학 조별 발표</div><div class="event-meta">과학실 · 2학년 3반</div></div><span class="event-badge">공유</span></article>
        <article class="event-card"><span class="event-time">16:00</span><div><div class="event-title">동아리 회의</div><div class="event-meta">동아리실</div></div><span class="event-badge is-personal">개인</span></article>
      </section>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 이미지 업로드 화면 HTML 문자열
// 기능: 공지 이미지 선택·촬영과 분석 시작 전 안내를 표시한다.
function renderUpload() {
  const hasPreview = Boolean(state.uploadImageUrl);

  return `
    <section class="upload-page">
      <header class="screen-header">
        <button class="back-button" type="button" aria-label="뒤로" data-action="back-home">${icon("back")}</button>
        <h1 class="screen-title">공지 사진으로 추가</h1>
        <span></span>
      </header>
      <section class="upload-box ${hasPreview ? "has-preview" : ""}">
        ${hasPreview ? `<div class="upload-preview-image"><img src="${state.uploadImageUrl}" alt="선택한 공지 이미지 미리보기" /></div><h2>사진을 확인해 주세요</h2><p>이 사진에서 일정 후보를 찾아 드려요</p>` : `<span class="card-icon">${icon("image")}</span><h2>공지 사진을 올려 주세요</h2><p>사진 속 날짜·장소·준비물을 찾아<br />일정 후보로 정리해 드려요</p>`}
        <div class="upload-actions"><button class="secondary-button" type="button" data-action="choose-image">${icon("image")} 사진 선택</button><button class="secondary-button" type="button" data-action="choose-camera">${icon("camera")} 촬영하기</button></div>
        <input id="image-file-input" class="visually-hidden" type="file" accept="image/jpeg,image/png" />
      </section>
      ${state.uploadError ? `<p class="upload-error" role="alert">${state.uploadError}</p>` : ""}
      <section class="upload-tips"><h2>이렇게 올리면 정확해요</h2><ul><li>글자가 잘 보이게 정면에서 찍어 주세요</li><li>한 장에 일정이 여러 개 있어도 괜찮아요</li><li>JPG, PNG · 최대 10MB</li></ul></section>
      <p class="upload-note">찾은 결과는 바로 등록되지 않아요. 확인 후 등록해요.</p>
      <div class="bottom-action"><button class="primary-button ${hasPreview ? "" : "is-disabled"}" type="button" data-action="start-analysis" ${hasPreview ? "" : "disabled"}>분석 시작</button></div>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 분석 중 화면 HTML 문자열
// 기능: 이미지 분석이 진행 중인 상태를 표시한다.
function renderAnalysisLoading() {
  return `
    <section class="analysis-page analysis-loading">
      <header class="screen-header"><button class="back-button" type="button" aria-label="뒤로" data-action="back-upload">${icon("back")}</button><h1 class="screen-title">일정 분석 중</h1><span></span></header>
      <div class="loading-panel"><span class="loading-spinner" aria-hidden="true"></span><h2>공지에서 일정을 찾고 있어요</h2><p>날짜·시간·장소·준비물을 확인하고 있어요</p></div>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 분석 결과 화면 HTML 문자열
// 기능: 여러 일정 후보와 날짜 확인이 필요한 부분 추출 상태를 표시한다.
function renderAnalysis() {
  return `
    <section class="analysis-page">
      <header class="screen-header"><button class="back-button" type="button" aria-label="뒤로" data-action="back-upload">${icon("back")}</button><h1 class="screen-title">분석 결과 확인</h1><button class="text-button" type="button">원본 보기</button></header>
      <h2 class="analysis-count">일정 3개를 찾았어요</h2>
      <p class="analysis-description">등록할 일정을 고르고 내용을 확인해 주세요</p>
      <div class="candidate-list">
        <article class="candidate-card"><div class="candidate-head"><span class="candidate-check">${icon("check")}</span><span class="candidate-title">수학 수행평가</span><button class="candidate-edit" type="button" data-action="edit-candidate">수정</button></div><div class="candidate-meta"><span>${icon("calendarSmall")}10월 29일 (목) · 3교시</span><span>${icon("image")}계산기</span></div></article>
        <article class="candidate-card"><div class="candidate-head"><span class="candidate-check">${icon("check")}</span><span class="candidate-title">과학 발표</span><button class="candidate-edit" type="button" data-action="edit-candidate">수정</button></div><div class="candidate-meta"><span>${icon("calendarSmall")}10월 27일 (화)</span><span>${icon("location")}과학실</span></div></article>
        <article class="candidate-card is-invalid"><div class="candidate-head"><span class="candidate-check">${icon("check")}</span><span class="candidate-title">영어 단어 시험</span><button class="candidate-edit" type="button" data-action="edit-candidate">수정</button></div><div class="candidate-meta"><span class="candidate-warning">${icon("calendarSmall")}날짜 확인 필요</span><span>${icon("image")}원문: 11월 첫째 주</span></div><div class="candidate-warning"><span>ⓘ 공지에 정확한 날짜가 없어요</span><button class="text-button" type="button" data-action="edit-candidate">날짜 입력</button></div></article>
      </div>
      <footer class="analysis-footer">${state.candidateDateConfirmed ? "" : "<p>ⓘ 확인이 필요한 일정이 1개 있어요</p>"}<button class="primary-button ${state.candidateDateConfirmed ? "" : "is-disabled"}" type="button" data-action="register-analysis" ${state.candidateDateConfirmed ? "" : "disabled"}>일정 3개 등록</button></footer>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 일정 후보 수정 화면 HTML 문자열
// 기능: 날짜가 모호한 후보를 사용자가 직접 수정하는 화면을 표시한다.
function renderCandidateEdit() {
  return `
    <section class="analysis-page analysis-modal-page">
      ${renderAnalysis()}
      <div class="modal-scrim" data-action="back-analysis"></div>
      <form class="edit-sheet" data-form="candidate-edit">
        <div class="sheet-handle" aria-hidden="true"></div>
        <header class="modal-header"><h2>추출 일정 수정</h2><button class="back-button" type="button" aria-label="닫기" data-action="back-analysis">×</button></header>
        <label class="form-field">일정 이름<input name="title" type="text" value="영어 단어 시험" /></label>
        <div class="form-row"><label class="form-field">날짜<input name="date" type="date" value="2026-11-05" required /></label><label class="form-field">시간<input name="time" type="time" value="09:00" /></label></div>
        <label class="form-field">장소<input name="location" type="text" placeholder="장소를 입력해 주세요" /></label>
        <button class="primary-button" type="submit">수정 저장</button>
      </form>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 일정 등록 완료 화면 HTML 문자열
// 기능: 분석한 일정이 등록된 뒤의 피그마 완료 팝업을 표시한다.
function renderRegistrationComplete() {
  return `
    <section class="analysis-page success-page">
      <div class="success-sheet" role="dialog" aria-labelledby="success-title">
        <span class="success-icon">${icon("check")}</span>
        <h1 id="success-title">일정 3개를 등록했어요</h1>
        <p>확인한 일정이 캘린더에 추가됐어요</p>
        <div class="success-list"><span>수학 수행평가 · 10월 29일</span><span>과학 발표 · 10월 27일</span><span>영어 단어 시험 · 11월 5일</span></div>
        <div class="success-actions"><button class="secondary-button" type="button" data-action="back-home">홈으로</button><button class="primary-button" type="button" data-action="open-calendar">캘린더에서 보기</button></div>
      </div>
    </section>
  `;
}

// 입력값: 없음
// 출력값: 그룹 목록 화면 HTML 문자열
// 기능: Figma의 공유 그룹 목록 화면을 표시한다.
function renderGroups() {
  const groups = [["2학년 3반", "반 · 구성원 28명 · 공유 일정 12개"], ["과학 발표 조", "조별 과제 · 구성원 4명 · 공유 일정 3개"], ["수학 스터디", "스터디 · 구성원 5명 · 공유 일정 6개"]];
  return `
    <section class="groups-page"><header class="screen-header"><button class="back-button" type="button" aria-label="뒤로" data-action="back-home">${icon("back")}</button><h1 class="screen-title">공유 그룹</h1><button class="icon-button" type="button" aria-label="그룹 추가">${icon("plus")}</button></header><div class="group-list">${groups.map(([name, meta]) => `<button class="group-card" type="button"><span class="group-icon">${icon("group")}</span><span><span class="group-name">${name}</span><span class="group-meta">${meta}</span></span><span class="chevron">${icon("chevron")}</span></button>`).join("")}</div><button class="group-create-button" type="button">＋ 그룹 만들기 · 참여하기</button></section>
  `;
}

// 입력값: 현재 화면 상태
// 출력값: 없음
// 기능: 현재 라우트에 해당하는 화면과 하단 네비게이션을 다시 그린다.
function renderApp() {
  const views = { home: renderHome, timetable: renderTimetable, timetableSettings: renderTimetableSettings, calendar: renderCalendar, upload: renderUpload, analysisLoading: renderAnalysisLoading, analysis: renderAnalysis, candidateEdit: renderCandidateEdit, registrationComplete: renderRegistrationComplete, groups: renderGroups };
  app.innerHTML = views[state.currentView]();
  document.querySelectorAll("[data-nav-icon]").forEach((element) => { element.innerHTML = icon(element.dataset.navIcon); });
  bottomNav.hidden = ["timetableSettings", "upload", "analysisLoading", "analysis", "candidateEdit", "registrationComplete"].includes(state.currentView);
  bottomNav.querySelectorAll("[data-route]").forEach((button) => { button.classList.toggle("is-active", button.dataset.route === state.currentView || (state.currentView === "groups" && button.dataset.route === "groups")); });
}

// 입력값: 클릭 이벤트
// 출력값: 없음
// 기능: 하단 네비게이션의 화면 전환을 처리한다.
function handleNavigation(event) {
  const button = event.target.closest("[data-route]");
  if (!button) return;
  state.currentView = button.dataset.route;
  renderApp();
}

// 입력값: 클릭 이벤트
// 출력값: 없음
// 기능: 화면 내 버튼의 이동과 임시 상호작용을 처리한다.
function handleAction(event) {
  const actionTarget = event.target.closest("[data-action]");
  const dateTarget = event.target.closest("[data-date]");
  const weekdayTarget = event.target.closest("[data-weekday]");
  if (dateTarget) {
    state.selectedDate = dateTarget.dataset.date;
    renderApp();
    return;
  }
  if (weekdayTarget) {
    state.selectedWeekday = weekdayTarget.dataset.weekday;
    renderApp();
    return;
  }
  if (!actionTarget) return;
  const { action } = actionTarget.dataset;
  if (action === "open-timetable" || action === "open-timetable-settings") state.currentView = action === "open-timetable" ? "timetable" : "timetableSettings";
  if (action === "back-timetable") state.currentView = "timetable";
  if (action === "save-timetable") { state.timetableHasClasses = true; state.currentView = "timetable"; }
  if (action === "open-upload") state.currentView = "upload";
  if (action === "choose-image" || action === "choose-camera") {
    const fileInput = document.querySelector("#image-file-input");
    if (fileInput) {
      fileInput.toggleAttribute("capture", action === "choose-camera");
      fileInput.click();
    }
    return;
  }
  if (action === "back-home") state.currentView = "home";
  if (action === "start-analysis") {
    state.currentView = "analysisLoading";
    state.analysisStatus = "loading";
    renderApp();
    window.setTimeout(() => {
      state.analysisStatus = "ready";
      state.currentView = "analysis";
      renderApp();
    }, 900);
    return;
  }
  if (action === "back-upload") state.currentView = "upload";
  if (action === "edit-candidate") state.currentView = "candidateEdit";
  if (action === "back-analysis") state.currentView = "analysis";
  if (action === "register-analysis") state.currentView = "registrationComplete";
  if (action === "open-calendar") state.currentView = "calendar";
  renderApp();
}

// 입력값: 일정 수정 폼 제출 이벤트
// 출력값: 없음
// 기능: 사용자가 입력한 날짜를 확인하고 분석 결과의 모호한 일정을 확정한다.
function handleFormSubmit(event) {
  const form = event.target.closest('[data-form="candidate-edit"]');
  if (!form) return;
  event.preventDefault();
  const date = form.elements.date.value;
  if (!date) return;
  state.candidateDateConfirmed = true;
  state.currentView = "analysis";
  renderApp();
}

// 입력값: 파일 선택 이벤트
// 출력값: 없음
// 기능: 이미지 형식과 크기를 확인한 뒤 선택한 파일을 업로드 화면에 미리 표시한다.
function handleImageChange(event) {
  const [file] = event.target.files;
  if (!file) return;

  if (!["image/jpeg", "image/png"].includes(file.type)) {
    state.uploadError = "JPG 또는 PNG 이미지만 올릴 수 있어요.";
    state.uploadImageUrl = "";
  } else if (file.size > 10 * 1024 * 1024) {
    state.uploadError = "이미지는 10MB 이하로 올려 주세요.";
    state.uploadImageUrl = "";
  } else {
    if (state.uploadImageUrl) URL.revokeObjectURL(state.uploadImageUrl);
    state.uploadError = "";
    state.uploadImageUrl = URL.createObjectURL(file);
  }

  renderApp();
}

bottomNav.addEventListener("click", handleNavigation);
app.addEventListener("click", handleAction);
app.addEventListener("change", handleImageChange);
app.addEventListener("submit", handleFormSubmit);
renderApp();
