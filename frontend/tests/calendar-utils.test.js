const test = require("node:test");
const assert = require("node:assert/strict");
const { buildCalendarDays, createMonthLabel, moveMonth } = require("../js/calendar-utils.js");

test("월간 캘린더는 일요일부터 시작하는 42개 날짜를 만든다", () => {
  const days = buildCalendarDays(2026, 8, new Date(2026, 8, 6));
  assert.equal(days.length, 42);
  assert.equal(days[0].isoDate, "2026-08-30");
  assert.equal(days[41].isoDate, "2026-10-10");
  assert.equal(days.find((day) => day.isToday).isoDate, "2026-09-06");
});

test("1월과 12월 경계에서 연도를 올바르게 이동한다", () => {
  const previousMonth = moveMonth(new Date(2026, 0, 1), -1);
  const nextMonth = moveMonth(new Date(2026, 11, 1), 1);
  assert.equal(createMonthLabel(previousMonth.getFullYear(), previousMonth.getMonth()), "2025년 12월");
  assert.equal(createMonthLabel(nextMonth.getFullYear(), nextMonth.getMonth()), "2027년 1월");
});
