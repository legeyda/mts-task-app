package com.legeyda.mts.util;

import java.util.function.Supplier;

public class CurrentTimeMillis implements Supplier<Long> {
	@Override
	public Long get() {
		return System.currentTimeMillis();
	}
}
