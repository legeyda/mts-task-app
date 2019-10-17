package com.legeyda.mts.model;

import java.time.Instant;

public class TaskImpl implements Task {

	private Status status;
	private Instant timestamp;

	public TaskImpl(Status status, Instant timestamp) {
		this.status = status;
		this.timestamp = timestamp;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public Instant getTimestamp() {
		return null;
	}
}
