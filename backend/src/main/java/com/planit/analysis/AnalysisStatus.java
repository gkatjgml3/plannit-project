package com.planit.analysis;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/** DB와 API에는 소문자 값(pending 등)으로 나간다. */
public enum AnalysisStatus {

	PENDING("pending"),
	PROCESSING("processing"),
	NEEDS_REVIEW("needs_review"),
	FAILED("failed"),
	CONFIRMED("confirmed");

	private final String value;

	AnalysisStatus(String value) {
		this.value = value;
	}

	public String value() {
		return value;
	}

	static AnalysisStatus fromValue(String value) {
		for (AnalysisStatus status : values()) {
			if (status.value.equals(value)) {
				return status;
			}
		}
		throw new IllegalArgumentException("알 수 없는 분석 상태: " + value);
	}

	@Converter
	static class DbConverter implements AttributeConverter<AnalysisStatus, String> {

		@Override
		public String convertToDatabaseColumn(AnalysisStatus status) {
			return status == null ? null : status.value;
		}

		@Override
		public AnalysisStatus convertToEntityAttribute(String value) {
			return value == null ? null : fromValue(value);
		}

	}

}
