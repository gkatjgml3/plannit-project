package com.planit.analysis.analyzer;

/** 분석기에 넘기는 값. 이미지 원본이 로그에 찍히지 않도록 toString에서 바이트 내용을 빼고 크기만 보여 준다. */
public record AnalysisInput(byte[] image, String contentType, String timezone) {

	@Override
	public String toString() {
		return "AnalysisInput[contentType=" + contentType + ", size=" + image.length + ", timezone=" + timezone + "]";
	}

}
