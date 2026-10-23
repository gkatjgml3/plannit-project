package com.planit.analysis.storage;

/**
 * 업로드한 이미지를 저장하는 곳의 경계. 저장 위치와 보관 기간은 아직 [미정]이므로
 * 로컬 파일·메모리 구현만 두고, 클라우드 저장소가 정해지면 구현을 추가해 바꾼다.
 * 키는 서버가 만든 임의 값이며 사용자가 올린 파일 이름을 쓰지 않는다.
 */
public interface ImageStorage {

	/** 입력값: 이미지 바이트, 검사한 MIME 타입 / 출력값: 다시 읽을 때 쓰는 키 / 기능: 이미지를 저장한다. */
	String store(byte[] data, String contentType);

	/** 입력값: store가 돌려준 키 / 출력값: 이미지 바이트 / 기능: 저장한 이미지를 읽는다. 없으면 ImageNotFoundException. */
	byte[] load(String key);

	/** 저장소에 이미지가 없을 때. */
	class ImageNotFoundException extends RuntimeException {

		public ImageNotFoundException() {
			super("저장된 이미지를 찾을 수 없습니다.");
		}

	}

}
