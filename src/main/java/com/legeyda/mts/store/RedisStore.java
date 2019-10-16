package com.legeyda.mts.store;

import java.util.Optional;
import java.util.function.Function;

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
