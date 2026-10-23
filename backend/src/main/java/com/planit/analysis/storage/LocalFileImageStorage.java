package com.planit.analysis.storage;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.nio.file.Files;
import java.nio.file.NoSuchFileException;
import java.nio.file.Path;
import java.util.UUID;
import java.util.regex.Pattern;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

/**
 * 서버 디스크의 한 폴더에 이미지를 저장한다. 개발용 기본 구현이며 planit.storage.type=local(기본값)일 때 쓴다.
 * 파일 이름은 서버가 만든 UUID라서 사용자가 경로를 조작할 수 없다.
 */
@Component
@ConditionalOnProperty(name = "planit.storage.type", havingValue = "local", matchIfMissing = true)
public class LocalFileImageStorage implements ImageStorage {

	private static final Pattern KEY_PATTERN = Pattern.compile("[0-9a-f-]{36}");

	private final Path directory;

	public LocalFileImageStorage(@Value("${planit.storage.local.directory:data/images}") String directory) {
		this.directory = Path.of(directory).toAbsolutePath().normalize();
	}

	@Override
	public String store(byte[] data, String contentType) {
		String key = UUID.randomUUID().toString();
		try {
			Files.createDirectories(directory);
			Files.write(pathOf(key), data);
			return key;
		}
		catch (IOException e) {
			throw new UncheckedIOException("이미지를 저장하지 못했습니다.", e);
		}
	}

	@Override
	public byte[] load(String key) {
		try {
			return Files.readAllBytes(pathOf(key));
		}
		catch (NoSuchFileException e) {
			throw new ImageNotFoundException();
		}
		catch (IOException e) {
			throw new UncheckedIOException("이미지를 읽지 못했습니다.", e);
		}
	}

	@Override
	public void delete(String key) {
		try {
			Files.deleteIfExists(pathOf(key));
		}
		catch (ImageNotFoundException e) {
			// 형식이 틀린 키는 처음부터 저장된 적이 없으므로 지울 것이 없다.
		}
		catch (IOException e) {
			throw new UncheckedIOException("이미지를 지우지 못했습니다.", e);
		}
	}

	private Path pathOf(String key) {
		if (!KEY_PATTERN.matcher(key).matches()) {
			throw new ImageNotFoundException();
		}
		return directory.resolve(key);
	}

}
