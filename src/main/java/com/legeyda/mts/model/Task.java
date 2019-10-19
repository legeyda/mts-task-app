package com.legeyda.mts.model;

import com.legeyda.mts.gen.model.TaskStatus;

import java.time.Instant;

/** наша внутрення модель, чтобы не делать слои логики и хранения зависящими от сгенерённого API */
public interface Task {
	enum Status { CREATED, RUNNING, FINISHED }

	Status getStatus();
	Instant getTimestamp();
}
