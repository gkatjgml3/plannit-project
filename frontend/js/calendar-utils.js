(function registerCalendarUtils(globalObject) {
  "use strict";

  function buildCalendarDays(year, monthIndex, today = new Date()) {
    const firstWeekday = new Date(year, monthIndex, 1).getDay();
    const daysInMonth = new Date(year, monthIndex + 1, 0).getDate();
    const visibleDayCount = Math.ceil((firstWeekday + daysInMonth) / 7) * 7;
    const gridStart = new Date(year, monthIndex, 1 - firstWeekday);

    return Array.from({ length: visibleDayCount }, (_, index) => {
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

  function moveMonth(date, amount) {
    return new Date(date.getFullYear(), date.getMonth() + amount, 1);
  }

  function createMonthLabel(year, monthIndex) {
    return `${year}년 ${monthIndex + 1}월`;
  }

  const calendarUtils = { buildCalendarDays, createMonthLabel, moveMonth };
  globalObject.PlanitCalendarUtils = calendarUtils;

  if (typeof module !== "undefined" && module.exports) {
    module.exports = calendarUtils;
  }
})(typeof window !== "undefined" ? window : globalThis);
