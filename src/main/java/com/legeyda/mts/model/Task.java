package com.legeyda.mts.model;

import java.time.Instant;

public interface Task {

	enum Status {
		created,
		running,
		finished
	}

	Status getStatus();

	Instant getTimestamp();
}
