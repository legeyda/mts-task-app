package com.legeyda.mts.store;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.function.Function;

@Component
@Profile("default")
public class RedisStore<K, V> implements Store<K, V> {

	private final MemoryStore<K, V> data = new MemoryStore<>();

	@Override
	public Optional<V> read(K key) {
		return data.read(key);
	}

	@Override
	public void write(K key, Function<Optional<V>, Optional<? extends V>> func) {
		data.write(key, func);
	}

}
