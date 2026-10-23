package com.planit.analysis;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

import com.planit.analysis.AnalysisCandidate.CandidateFields;
import com.planit.analysis.AnalysisCandidate.CandidateRecurrence;
import com.planit.analysis.AnalysisDtos.CandidateError;
import com.planit.analysis.analyzer.AnalyzerResult.CandidateDraft;
import com.planit.analysis.analyzer.AnalyzerResult.RecurrenceDraft;
import com.planit.event.EventService.NewEventData;
import com.planit.event.RecurrenceRule;

/**
 * 후보에 대한 규칙 모음.
 * - sanitize: 분석기 결과를 저장 가능한 후보로 정리한다. 값을 새로 지어내지 않고, 길이를 맞추거나 알 수 없는 값을 버린다.
 * - toEventData: 확정 직전에 실제 달력·시간·반복 규칙을 검사해 일정으로 저장할 값을 만든다.
 */
final class CandidateRules {

	static final int MAX_CANDIDATES = 50;
	private static final int MAX_WARNINGS = 10;
	private static final int MAX_WARNING_LENGTH = 150;
	private static final int MAX_MATERIALS = 20;
	private static final int MAX_MATERIAL_LENGTH = 80;
	private static final int MAX_DESCRIPTION = 2000;
	private static final int MAX_BY_DAYS = 7;

	/** 2026-02-30처럼 달력에 없는 날짜를 거부하도록 STRICT로 해석한다. */
	private static final DateTimeFormatter DATE = DateTimeFormatter.ofPattern("uuuu-MM-dd")
			.withResolverStyle(ResolverStyle.STRICT);
	private static final DateTimeFormatter TIME = DateTimeFormatter.ofPattern("HH:mm")
			.withResolverStyle(ResolverStyle.STRICT);

	private CandidateRules() {
	}

	/** 분석기 결과 하나를 정리한 값. */
	record SanitizedCandidate(CandidateFields fields, List<String> uncertainFields, List<String> warnings) {
	}

	/**
	 * 입력값: 분석기가 준 후보, 분석의 기본 시간대
	 * 출력값: 저장할 후보 값과 모호한 필드·경고
	 * 기능: 빈 값은 null로, 너무 긴 값은 잘라서 경고를 남기고, 모르는 필드 이름은 버린다. 이미지에 없는 값을 채우지 않는다.
	 */
	static SanitizedCandidate sanitize(CandidateDraft draft, String defaultTimezone) {
		List<String> warnings = new ArrayList<>();
		Set<String> uncertain = new LinkedHashSet<>();
		if (draft.uncertainFields() != null) {
			draft.uncertainFields().stream().filter(AnalysisCandidate.FIELDS::contains).forEach(uncertain::add);
		}

		String timezone = clean(draft.timezone(), 64, "timezone", warnings);
		if (timezone == null) {
			timezone = defaultTimezone;
		}
		else if (!isValidZone(timezone)) {
			warnings.add("알 수 없는 시간대를 기본 시간대로 바꿨습니다: " + timezone);
			uncertain.add("timezone");
			timezone = defaultTimezone;
		}

		String startDate = clean(draft.startDate(), 32, "startDate", warnings);
		String endDate = clean(draft.endDate(), 32, "endDate", warnings);
		warnIfInvalidDate(startDate, warnings);
		warnIfInvalidDate(endDate, warnings);

		List<String> materials = new ArrayList<>();
		if (draft.materials() != null) {
			for (String material : draft.materials()) {
				String cleaned = clean(material, MAX_MATERIAL_LENGTH, "materials", warnings);
				if (cleaned != null && materials.size() < MAX_MATERIALS) {
					materials.add(cleaned);
				}
			}
		}

		CandidateFields fields = new CandidateFields(
				clean(draft.title(), 200, "title", warnings),
				startDate,
				clean(draft.startTime(), 16, "startTime", warnings),
				endDate,
				clean(draft.endTime(), 16, "endTime", warnings),
				timezone,
				clean(draft.location(), 255, "location", warnings),
				materials,
				clean(draft.description(), MAX_DESCRIPTION, "description", warnings),
				draft.recurring(),
				clean(draft.recurrenceNote(), 255, "recurrenceNote", warnings),
				sanitizeRecurrence(draft.recurrence(), warnings));
		if (draft.warnings() != null) {
			warnings.addAll(0, draft.warnings());
		}
		return new SanitizedCandidate(fields, List.copyOf(uncertain), limitWarnings(warnings));
	}

	/** 입력값: 분석기가 읽은 반복 규칙 / 출력값: 열 크기에 맞춘 값, 없으면 null / 기능: 값을 고치지 않고 길이만 맞춘다. */
	static CandidateRecurrence sanitizeRecurrence(RecurrenceDraft draft, List<String> warnings) {
		if (draft == null) {
			return null;
		}
		String frequency = clean(draft.frequency(), 16, "recurrence.frequency", warnings);
		List<String> byDays = new ArrayList<>();
		if (draft.byDays() != null) {
			for (String day : draft.byDays()) {
				String cleaned = clean(day, 2, "recurrence.byDays", warnings);
				if (cleaned != null && byDays.size() < MAX_BY_DAYS) {
					byDays.add(cleaned.toUpperCase(Locale.ROOT));
				}
			}
		}
		return new CandidateRecurrence(frequency == null ? null : frequency.toUpperCase(Locale.ROOT),
				draft.interval(), clean(draft.until(), 32, "recurrence.until", warnings), draft.count(),
				List.copyOf(byDays));
	}

	/** 입력값: 경고 목록 / 출력값: 개수·길이를 제한한 목록 / 기능: DB 열 크기를 넘지 않게 자른다. */
	static List<String> limitWarnings(List<String> warnings) {
		if (warnings == null) {
			return List.of();
		}
		return warnings.stream()
				.filter(w -> w != null && !w.isBlank())
				.map(w -> w.length() > MAX_WARNING_LENGTH ? w.substring(0, MAX_WARNING_LENGTH) : w)
				.limit(MAX_WARNINGS)
				.toList();
	}

	/**
	 * 입력값: 확정하려는 후보, 오류를 모을 목록
	 * 출력값: 저장할 일정 값. 규칙을 어기면 null을 돌려주고 errors에 이유를 추가한다.
	 * 기능: 제목·날짜 필수, 모호한 필드 없음, 실제 달력 날짜, 시작 < 종료, 반복 규칙을 검사한다.
	 * 준비물은 일정의 준비물(checklist)로, 반복 규칙은 일정의 반복 규칙으로 저장한다.
	 */
	static NewEventData toEventData(AnalysisCandidate candidate, List<CandidateError> errors) {
		Long id = candidate.getId();
		CandidateFields f = candidate.fields();

		List<String> uncertainDates = candidate.getUncertainFields().stream()
				.filter(AnalysisCandidate.DATE_FIELDS::contains).toList();
		if (!uncertainDates.isEmpty()) {
			return reject(errors, id, "CANDIDATE_DATE_UNCERTAIN", "날짜·시간이 모호합니다. 확인하거나 고친 뒤 확정하세요: " + uncertainDates);
		}
		if (!candidate.getUncertainFields().isEmpty()) {
			return reject(errors, id, "CANDIDATE_FIELD_UNCERTAIN",
					"확인하지 않은 모호한 값이 있습니다: " + candidate.getUncertainFields());
		}
		if (f.title() == null || f.title().isBlank()) {
			return reject(errors, id, "CANDIDATE_TITLE_MISSING", "제목이 없습니다.");
		}
		if (f.startDate() == null) {
			return reject(errors, id, "CANDIDATE_DATE_MISSING", "날짜가 없습니다. 날짜를 입력한 뒤 확정하세요.");
		}
		ZoneId zone;
		try {
			zone = ZoneId.of(f.timezone());
		}
		catch (DateTimeException e) {
			return reject(errors, id, "INVALID_TIMEZONE", "알 수 없는 시간대입니다: " + f.timezone());
		}
		LocalDate startDate = parseDate(f.startDate());
		LocalDate endDate = f.endDate() == null ? startDate : parseDate(f.endDate());
		if (startDate == null || endDate == null) {
			return reject(errors, id, "INVALID_DATE", "존재하지 않는 날짜이거나 형식(yyyy-MM-dd)이 틀렸습니다.");
		}

		ZonedDateTime start;
		ZonedDateTime end;
		if (f.startTime() == null && f.endTime() == null) {
			// 시간이 없으면 종일 일정: 시작일 00:00부터 종료일 다음 날 00:00까지
			start = startDate.atStartOfDay(zone);
			end = endDate.plusDays(1).atStartOfDay(zone);
		}
		else if (f.startTime() == null || f.endTime() == null) {
			return reject(errors, id, "CANDIDATE_TIME_INCOMPLETE",
					"시작·종료 시간을 모두 입력하거나, 종일 일정이면 둘 다 비워 두세요.");
		}
		else {
			LocalTime startTime = parseTime(f.startTime());
			LocalTime endTime = parseTime(f.endTime());
			if (startTime == null || endTime == null) {
				return reject(errors, id, "INVALID_TIME", "시간 형식(HH:mm)이 틀렸습니다.");
			}
			start = ZonedDateTime.of(startDate, startTime, zone);
			end = ZonedDateTime.of(endDate, endTime, zone);
		}
		if (!end.isAfter(start)) {
			return reject(errors, id, "INVALID_TIME_RANGE", "종료 시간은 시작 시간보다 늦어야 합니다.");
		}

		RecurrenceRule rule = null;
		CandidateRecurrence r = f.recurrence();
		if (r != null) {
			try {
				rule = RecurrenceRule.parse(r.frequency(), r.interval(), r.until(), r.count(), r.byDays(), startDate);
			}
			catch (RecurrenceRule.InvalidRecurrenceException e) {
				return reject(errors, id, "CANDIDATE_RECURRENCE_INVALID", "반복 규칙이 올바르지 않습니다: " + e.getMessage());
			}
		}
		else if (f.recurring()) {
			return reject(errors, id, "CANDIDATE_RECURRENCE_MISSING",
					"반복 일정이지만 반복 규칙이 없습니다. 규칙을 입력하거나 반복하지 않는 일정으로 바꾸세요.");
		}
		return new NewEventData(f.title().trim(), start.toInstant(), end.toInstant(), f.timezone(), f.description(),
				f.location(), f.materials(), rule);
	}

	static boolean isValidZone(String timezone) {
		try {
			ZoneId.of(timezone);
			return true;
		}
		catch (DateTimeException e) {
			return false;
		}
	}

	/** 입력값: 문자열 / 출력값: 앞뒤 공백을 지운 값, 비었으면 null / 기능: 최대 길이를 넘으면 자르고 경고를 남긴다. */
	static String clean(String value, int maxLength, String field, List<String> warnings) {
		if (value == null || value.isBlank()) {
			return null;
		}
		String trimmed = value.trim();
		if (trimmed.length() > maxLength) {
			warnings.add(field + " 값이 너무 길어 " + maxLength + "자로 잘랐습니다.");
			return trimmed.substring(0, maxLength);
		}
		return trimmed;
	}

	private static void warnIfInvalidDate(String date, List<String> warnings) {
		if (date != null && parseDate(date) == null) {
			warnings.add("존재하지 않는 날짜이거나 형식이 틀렸습니다: " + date);
		}
	}

	private static LocalDate parseDate(String value) {
		try {
			return LocalDate.parse(value, DATE);
		}
		catch (DateTimeParseException e) {
			return null;
		}
	}

	private static LocalTime parseTime(String value) {
		try {
			return LocalTime.parse(value, TIME);
		}
		catch (DateTimeParseException e) {
			return null;
		}
	}

	private static NewEventData reject(List<CandidateError> errors, Long candidateId, String code, String message) {
		errors.add(new CandidateError(candidateId, code, message));
		return null;
	}

}
