package com.planit.analysis;

import java.util.List;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import tools.jackson.core.type.TypeReference;
import tools.jackson.databind.json.JsonMapper;

/** 문자열 목록(준비물, 모호한 필드, 경고)을 JSON 배열 문자열로 저장한다. */
@Converter
class StringListConverter implements AttributeConverter<List<String>, String> {

	private static final JsonMapper MAPPER = JsonMapper.builder().build();
	private static final TypeReference<List<String>> TYPE = new TypeReference<>() {
	};

	@Override
	public String convertToDatabaseColumn(List<String> values) {
		return values == null || values.isEmpty() ? null : MAPPER.writeValueAsString(values);
	}

	@Override
	public List<String> convertToEntityAttribute(String json) {
		return json == null || json.isBlank() ? List.of() : List.copyOf(MAPPER.readValue(json, TYPE));
	}

}
