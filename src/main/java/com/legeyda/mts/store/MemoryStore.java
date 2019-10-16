package com.legeyda.mts.store;

import com.legeyda.mts.model.Task;

import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

public class MemoryStore<K, V> implements Store<K, V> {

	private final ConcurrentHashMap<K, V> data = new ConcurrentHashMap<>();

	@Override
	public Optional<V> read(K key) {
		return Optional.ofNullable(this.data.get(key));
	}

	@Override
	public void write(K key, Function<Optional<V>, Optional<? extends V>> func) {
		this.data.compute(key, (K theSameKey, V value) ->
				func.apply(Optional.ofNullable(value)).orElse(null));
	}

}
