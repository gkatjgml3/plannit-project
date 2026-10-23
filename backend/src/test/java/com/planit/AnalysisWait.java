package com.planit;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

import com.jayway.jsonpath.JsonPath;

import org.springframework.test.web.servlet.MockMvc;

/** 분석이 pending·processing을 벗어날 때까지 기다리는 테스트 도우미. */
final class AnalysisWait {

	private AnalysisWait() {
	}

	/** 입력값: MockMvc, Authorization 값, 분석 id / 출력값: 끝난 상태 / 기능: 최대 10초 동안 상태를 조회하며 기다린다. */
	static String untilFinished(MockMvc mvc, String bearer, long analysisId) throws Exception {
		long deadline = System.currentTimeMillis() + 10_000;
		while (true) {
			String body = mvc.perform(get("/api/analyses/{id}", analysisId).header("Authorization", bearer))
					.andReturn().getResponse().getContentAsString();
			String status = JsonPath.read(body, "$.status");
			if (!status.equals("pending") && !status.equals("processing")) {
				return status;
			}
			if (System.currentTimeMillis() > deadline) {
				throw new AssertionError("분석이 10초 안에 끝나지 않았습니다. 현재 상태: " + status);
			}
			Thread.sleep(20);
		}
	}

}
