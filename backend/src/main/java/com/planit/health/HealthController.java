package com.planit.health;

import java.util.Map;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.ExampleObject;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;

import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@Tag(name = "상태")
@SecurityRequirements
public class HealthController {

	private final JdbcTemplate jdbcTemplate;

	public HealthController(JdbcTemplate jdbcTemplate) {
		this.jdbcTemplate = jdbcTemplate;
	}

	@GetMapping("/health")
	@Operation(summary = "서버·DB 상태", description = "DB에 select 1을 보내 확인한다.")
	@ApiResponse(responseCode = "200", content = @Content(examples = @ExampleObject(value = "{\"status\": \"UP\", \"db\": \"UP\"}")))
	@ApiResponse(responseCode = "503", content = @Content(examples = @ExampleObject(value = "{\"status\": \"DOWN\", \"db\": \"DOWN\"}")))
	public ResponseEntity<Map<String, String>> health() {
		try {
			jdbcTemplate.queryForObject("select 1", Integer.class);
			return ResponseEntity.ok(Map.of("status", "UP", "db", "UP"));
		}
		catch (DataAccessException e) {
			return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(Map.of("status", "DOWN", "db", "DOWN"));
		}
	}

}
