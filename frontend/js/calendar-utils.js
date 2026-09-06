(function registerCalendarUtils(globalObject) {
  "use strict";

  // 입력값: 연도와 0부터 시작하는 월
  // 출력값: 해당 월을 포함하는 6주 분량의 날짜 정보 42개
  // 기능: 이전 달과 다음 달 날짜를 포함한 월간 캘린더 칸을 계산한다.
  function buildCalendarDays(year, monthIndex, today = new Date()) {
    const firstWeekday = new Date(year, monthIndex, 1).getDay();
    const gridStart = new Date(year, monthIndex, 1 - firstWeekday);

    return Array.from({ length: 42 }, (_, index) => {
      const date = new Date(gridStart.getFullYear(), gridStart.getMonth(), gridStart.getDate() + index);
      const dateYear = date.getFullYear();
      const dateMonthIndex = date.getMonth();
      const dateDay = date.getDate();
      return {
        year: dateYear,
        monthIndex: dateMonthIndex,
        day: dateDay,
        isoDate: `${dateYear}-${String(dateMonthIndex + 1).padStart(2, "0")}-${String(dateDay).padStart(2, "0")}`,
        isOtherMonth: dateMonthIndex !== monthIndex,
        isToday: dateYear === today.getFullYear() && dateMonthIndex === today.getMonth() && dateDay === today.getDate(),
      };
    });
  }

  // 입력값: 기준 날짜와 이동할 월 수
  // 출력값: 이동한 월의 1일 날짜
  // 기능: 1월과 12월 경계를 포함해 이전 달과 다음 달을 계산한다.
  function moveMonth(date, amount) {
    return new Date(date.getFullYear(), date.getMonth() + amount, 1);
  }

  // 입력값: 연도와 0부터 시작하는 월
  // 출력값: 화면에 표시할 한국어 연월 문자열
  // 기능: 캘린더 제목을 연도와 월에 맞게 만든다.
  function createMonthLabel(year, monthIndex) {
    return `${year}년 ${monthIndex + 1}월`;
  }

  const calendarUtils = { buildCalendarDays, createMonthLabel, moveMonth };
  globalObject.PlanitCalendarUtils = calendarUtils;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = calendarUtils;
  }
})(typeof window !== "undefined" ? window : globalThis);
