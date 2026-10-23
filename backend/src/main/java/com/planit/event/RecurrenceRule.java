package com.planit.event;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 검증을 마친 반복 규칙. 지원 범위: DAILY·WEEKLY·MONTHLY, interval, until(그날 포함) 또는 count, WEEKLY 요일.
 * MONTHLY는 시작일과 같은 날짜에 반복하며, 그 날짜가 없는 달(예: 31일)은 건너뛰고 횟수에도 세지 않는다.
 */
public record RecurrenceRule(Frequency frequency, int interval, LocalDate until, Integer count,
		List<DayOfWeek> byDays) {

	public enum Frequency {
		DAILY, WEEKLY, MONTHLY
	}

	static final int MAX_INTERVAL = 365;
	static final int MAX_COUNT = 1000;
	/** 펼칠 때 살펴보는 날짜 수의 상한. 잘못된 규칙이 서버를 오래 붙잡지 않게 한다. */
	private static final int MAX_ITERATIONS = 100_000;

	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
			.withResolverStyle(ResolverStyle.STRICT);
	private static final Map<String, DayOfWeek> DAY_CODES = Map.of("MO", DayOfWeek.MONDAY, "TU", DayOfWeek.TUESDAY,
			"WE", DayOfWeek.WEDNESDAY, "TH", DayOfWeek.THURSDAY, "FR", DayOfWeek.FRIDAY, "SA", DayOfWeek.SATURDAY,
			"SU", DayOfWeek.SUNDAY);

	/** 규칙이 맞지 않을 때. 메시지는 사용자에게 보여 줄 문장이다. */
	public static class InvalidRecurrenceException extends RuntimeException {

		public InvalidRecurrenceException(String message) {
			super(message);
		}

	}

	/**
	 * 입력값: 요청에 들어온 값(검증 전), 일정 시작 날짜(일정 timezone 기준)
	 * 출력값: 검증한 반복 규칙
	 * 기능: 빈도·간격·종료일·횟수·요일을 검사한다. until과 count는 함께 쓸 수 없고, WEEKLY 요일에는 시작 요일이 들어 있어야 한다.
	 * 요일을 비우면 시작 요일 하나로 본다.
	 */
	public static RecurrenceRule parse(String frequency, Integer interval, String until, Integer count,
			List<String> byDays, LocalDate startDate) {
		Frequency freq;
		try {
			freq = Frequency.valueOf(frequency == null ? "" : frequency.trim());
		}
		catch (IllegalArgumentException e) {
			throw new InvalidRecurrenceException("frequency는 DAILY, WEEKLY, MONTHLY 중 하나여야 합니다.");
		}
		int step = interval == null ? 1 : interval;
		if (step < 1 || step > MAX_INTERVAL) {
			throw new InvalidRecurrenceException("interval은 1 이상 " + MAX_INTERVAL + " 이하여야 합니다.");
		}
		if (until != null && count != null) {
			throw new InvalidRecurrenceException("until과 count는 함께 쓸 수 없습니다.");
		}
		LocalDate untilDate = null;
		if (until != null) {
			try {
				untilDate = LocalDate.parse(until.trim(), DATE);
			}
			catch (DateTimeParseException e) {
				throw new InvalidRecurrenceException("until은 실제로 있는 날짜(yyyy-MM-dd)여야 합니다.");
			}
			if (untilDate.isBefore(startDate)) {
				throw new InvalidRecurrenceException("until은 일정 시작 날짜와 같거나 뒤여야 합니다.");
			}
		}
		if (count != null && (count < 1 || count > MAX_COUNT)) {
			throw new InvalidRecurrenceException("count는 1 이상 " + MAX_COUNT + " 이하여야 합니다.");
		}

		List<DayOfWeek> days = List.of();
		if (byDays != null && !byDays.isEmpty()) {
			if (freq != Frequency.WEEKLY) {
				throw new InvalidRecurrenceException("byDays는 WEEKLY에서만 쓸 수 있습니다.");
			}
			EnumSet<DayOfWeek> set = EnumSet.noneOf(DayOfWeek.class);
			for (String code : byDays) {
				DayOfWeek day = code == null ? null : DAY_CODES.get(code.trim());
				if (day == null) {
					throw new InvalidRecurrenceException("byDays는 MO, TU, WE, TH, FR, SA, SU만 쓸 수 있습니다.");
				}
				if (!set.add(day)) {
					throw new InvalidRecurrenceException("byDays에 같은 요일이 두 번 들어 있습니다.");
				}
			}
			if (!set.contains(startDate.getDayOfWeek())) {
				throw new InvalidRecurrenceException("byDays에는 일정 시작 날짜의 요일이 들어 있어야 합니다.");
			}
			days = List.copyOf(set);
		}
		else if (freq == Frequency.WEEKLY) {
			days = List.of(startDate.getDayOfWeek());
		}
		return new RecurrenceRule(freq, step, untilDate, count, days);
	}

	/**
	 * 입력값: 원본 일정의 시작·종료 시각, 일정 timezone, 조회 구간 [from, to)
	 * 출력값: 구간과 겹치는 회차의 시작 시각 목록(시간 순)
	 * 기능: timezone의 현지 시각을 기준으로 펼친다. 매주 월요일 09:00(현지)은 서머타임으로 UTC가 바뀌어도 09:00으로 유지된다.
	 * 회차 길이도 현지 시각 기준 길이를 쓴다.
	 */
	public List<Occurrence> expand(Instant seriesStart, Instant seriesEnd, ZoneId zone, Instant from, Instant to) {
		LocalDateTime localStart = seriesStart.atZone(zone).toLocalDateTime();
		Duration localLength = Duration.between(localStart, seriesEnd.atZone(zone).toLocalDateTime());
		LocalDate startDate = localStart.toLocalDate();

		List<Occurrence> result = new ArrayList<>();
		int generated = 0;
		for (int i = 0; i < MAX_ITERATIONS; i++) {
			List<LocalDate> dates = datesInStep(startDate, i);
			boolean finished = false;
			for (LocalDate date : dates) {
				if (until != null && date.isAfter(until)) {
					finished = true;
					break;
				}
				generated++;
				if (count != null && generated > count) {
					finished = true;
					break;
				}
				ZonedDateTime occurrenceStart = ZonedDateTime.of(date.atTime(localStart.toLocalTime()), zone);
				if (!occurrenceStart.toInstant().isBefore(to)) {
					finished = true;
					break;
				}
				ZonedDateTime occurrenceEnd = ZonedDateTime.of(date.atTime(localStart.toLocalTime()).plus(localLength),
						zone);
				if (occurrenceEnd.toInstant().isAfter(from)) {
					result.add(new Occurrence(occurrenceStart, occurrenceEnd));
				}
			}
			if (finished) {
				break;
			}
		}
		return result;
	}

	/** 펼친 회차 하나. */
	public record Occurrence(ZonedDateTime start, ZonedDateTime end) {
	}

	/** i번째 반복 단계에 들어가는 날짜들. MONTHLY에서 없는 날짜인 달은 빈 목록이다. */
	private List<LocalDate> datesInStep(LocalDate startDate, int step) {
		switch (frequency) {
			case DAILY:
				return List.of(startDate.plusDays((long) step * interval));
			case WEEKLY: {
				LocalDate weekStart = startDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY))
						.plusWeeks((long) step * interval);
				return byDays.stream()
						.sorted(Comparator.naturalOrder())
						.map(day -> weekStart.plusDays(day.getValue() - 1L))
						.filter(date -> !date.isBefore(startDate))
						.toList();
			}
			case MONTHLY: {
				YearMonth month = YearMonth.from(startDate).plusMonths((long) step * interval);
				int day = startDate.getDayOfMonth();
				return day <= month.lengthOfMonth() ? List.of(month.atDay(day)) : List.of();
			}
			default:
				throw new IllegalStateException();
		}
	}

	/** 입력값: 없음 / 출력값: "MO,WE" 형태 문자열, 요일이 없으면 null / 기능: DB 열에 저장할 값. */
	String byDaysColumn() {
		if (byDays.isEmpty()) {
			return null;
		}
		return byDays.stream().sorted().map(RecurrenceRule::code).collect(Collectors.joining(","));
	}

	/** 입력값: 없음 / 출력값: API에 내보낼 요일 코드 목록 / 기능: 요일을 MO..SU 코드로 바꾼다. */
	public List<String> byDayCodes() {
		return byDays.stream().sorted().map(RecurrenceRule::code).toList();
	}

	/** 입력값: 없음 / 출력값: 규칙을 한 줄로 쓴 문자열 / 기능: 요청 지문 계산에 쓴다. */
	String canonical() {
		return frequency + ";" + interval + ";" + until + ";" + count + ";" + byDaysColumn();
	}

	static RecurrenceRule fromColumns(String frequency, Integer interval, LocalDate until, Integer count,
			String byDays) {
		if (frequency == null) {
			return null;
		}
		List<DayOfWeek> days = byDays == null || byDays.isBlank() ? List.of()
				: java.util.Arrays.stream(byDays.split(",")).map(DAY_CODES::get).toList();
		return new RecurrenceRule(Frequency.valueOf(frequency), interval == null ? 1 : interval, until, count, days);
	}

	private static String code(DayOfWeek day) {
		return day.name().substring(0, 2);
	}

}
