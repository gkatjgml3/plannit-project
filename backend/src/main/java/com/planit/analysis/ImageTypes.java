package com.planit.analysis;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

/** 클라이언트가 보낸 Content-Type을 믿지 않고, 파일 앞부분의 시그니처로 이미지 형식을 확인한다. */
final class ImageTypes {

	private static final byte[] PNG = { (byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n' };

	private ImageTypes() {
	}

	/** 입력값: 파일 바이트 / 출력값: image/jpeg·image/png·image/webp 중 하나, 지원하지 않으면 빈 값 / 기능: 이미지 형식 판별. */
	static Optional<String> detect(byte[] data) {
		if (data.length >= 3 && (data[0] & 0xFF) == 0xFF && (data[1] & 0xFF) == 0xD8 && (data[2] & 0xFF) == 0xFF) {
			return Optional.of("image/jpeg");
		}
		if (startsWith(data, PNG)) {
			return Optional.of("image/png");
		}
		if (data.length >= 12 && ascii(data, 0, 4).equals("RIFF") && ascii(data, 8, 4).equals("WEBP")) {
			return Optional.of("image/webp");
		}
		return Optional.empty();
	}

	private static boolean startsWith(byte[] data, byte[] prefix) {
		if (data.length < prefix.length) {
			return false;
		}
		for (int i = 0; i < prefix.length; i++) {
			if (data[i] != prefix[i]) {
				return false;
			}
		}
		return true;
	}

	private static String ascii(byte[] data, int offset, int length) {
		return new String(data, offset, length, StandardCharsets.US_ASCII);
	}

}
