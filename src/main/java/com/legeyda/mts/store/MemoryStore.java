package com.legeyda.mts.store;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;

@Component
@Profile("test")
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
