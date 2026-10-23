package com.planit.common;

import java.util.stream.Collectors;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import org.springframework.web.multipart.support.MissingServletRequestPartException;

/** 모든 오류 응답을 {code, message} 형태로 통일한다. */
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(ApiException.class)
	public ResponseEntity<ErrorResponse> handleApi(ApiException e) {
		return ResponseEntity.status(e.getStatus()).body(new ErrorResponse(e.getCode(), e.getMessage()));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleValidation(MethodArgumentNotValidException e) {
		String message = e.getBindingResult().getFieldErrors().stream()
				.map(error -> error.getField() + ": " + error.getDefaultMessage())
				.collect(Collectors.joining(", "));
		return badRequest("VALIDATION_FAILED", message);
	}

	/** JSON 형식이 틀렸거나 2월 30일처럼 존재하지 않는 날짜가 들어온 경우. */
	@ExceptionHandler(HttpMessageNotReadableException.class)
	public ResponseEntity<ErrorResponse> handleUnreadable(HttpMessageNotReadableException e) {
		return badRequest("INVALID_REQUEST_BODY", "요청 본문을 읽을 수 없습니다. 날짜·시간 형식을 확인하세요.");
	}

	@ExceptionHandler({ MissingServletRequestParameterException.class, MissingRequestHeaderException.class,
			MethodArgumentTypeMismatchException.class, MissingServletRequestPartException.class })
	public ResponseEntity<ErrorResponse> handleBadParameter(Exception e) {
		return badRequest("INVALID_PARAMETER", "요청 값이 없거나 형식이 잘못되었습니다.");
	}

	@ExceptionHandler(MaxUploadSizeExceededException.class)
	public ResponseEntity<ErrorResponse> handleTooLarge(MaxUploadSizeExceededException e) {
		return ResponseEntity.status(HttpStatus.CONTENT_TOO_LARGE)
				.body(new ErrorResponse("IMAGE_TOO_LARGE", "업로드한 파일이 너무 큽니다."));
	}

	@ExceptionHandler(ObjectOptimisticLockingFailureException.class)
	public ResponseEntity<ErrorResponse> handleOptimisticLock(ObjectOptimisticLockingFailureException e) {
		return ResponseEntity.status(HttpStatus.CONFLICT)
				.body(new ErrorResponse("VERSION_CONFLICT", "다른 곳에서 먼저 수정되었습니다. 새로 불러온 뒤 다시 시도하세요."));
	}

	private ResponseEntity<ErrorResponse> badRequest(String code, String message) {
		return ResponseEntity.badRequest().body(new ErrorResponse(code, message));
	}

}
