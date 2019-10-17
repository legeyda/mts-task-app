package com.legeyda.mts.util;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.function.Supplier;

@Component
@Profile("default")
public class CurrentTime implements Supplier<Instant> {
	@Override
	public Instant get() {
		return Instant.now();
	}
}
