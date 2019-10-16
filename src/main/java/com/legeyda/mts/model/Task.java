package com.legeyda.mts.model;

import java.util.Date;

public interface Task {

	enum Status {
		created,
		running,
		finished
	}

	Status getStatus();

	Date getTimestamp();
}
