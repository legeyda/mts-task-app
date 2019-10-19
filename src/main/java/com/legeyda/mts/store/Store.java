package com.legeyda.mts.store;

import java.util.Optional;
import java.util.function.Function;

public interface Store<K, V> {

	Optional<V> read(K key);

	/** атомарно вычислить новое значение на основе текущего значения */
	void write(K key, Function<Optional<V>, Optional<? extends V>> func);

}
