package com.legeyda.mts.store;

import org.junit.Test;

import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

public class MemoryStoreTest {
	@Test
	public void test() {
		final MemoryStore<String, String> testee = new MemoryStore<>();
		assertThat(testee.read("123")).isNotPresent();

		testee.write("123", (Optional<String> oldValue) -> Optional.of("hello"));
		assertThat(testee.read("123")).isPresent();
		assertThat(testee.read("123")).contains("hello");

		testee.write("123", (Optional<String> oldValue) -> Optional.of("hello2"));
		assertThat(testee.read("123")).isPresent();
		assertThat(testee.read("123")).contains("hello2");
	}


}
