package com.legeyda.mts.model;

import com.legeyda.mts.gen.model.TaskStatus;

import java.time.Instant;

public interface Task {

	enum Status {
		created(TaskStatus.StatusEnum.CREATED),
		running(TaskStatus.StatusEnum.RUNNING),
		finished(TaskStatus.StatusEnum.FINISHED);


		private final TaskStatus.StatusEnum genStatus;
		Status(TaskStatus.StatusEnum genStatus) {
			this.genStatus = genStatus;
		}

		public TaskStatus.StatusEnum getGenStatus() {
			return genStatus;
		}
	}

	Status getStatus();

	Instant getTimestamp();
}
