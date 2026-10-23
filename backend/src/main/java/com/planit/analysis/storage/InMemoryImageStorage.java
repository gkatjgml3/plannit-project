package com.planit.analysis.storage;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/** 테스트용 메모리 저장소. 서버를 끄면 사라진다. planit.storage.type=memory일 때 쓴다. */
@Component
@ConditionalOnProperty(name = "planit.storage.type", havingValue = "memory")
public class InMemoryImageStorage implements ImageStorage {

	private final Map<String, byte[]> images = new ConcurrentHashMap<>();

	@Override
	public String store(byte[] data, String contentType) {
		String key = UUID.randomUUID().toString();
		images.put(key, data.clone());
		return key;
	}

	@Override
	public byte[] load(String key) {
		byte[] data = images.get(key);
		if (data == null) {
			throw new ImageNotFoundException();
		}
		return data.clone();
	}

}
