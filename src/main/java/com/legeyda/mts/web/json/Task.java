package com.legeyda.mts.web.json;

import java.sql.Timestamp;
import java.util.Date;

public class Task {

	public String status;
	public Date timestamp;

	public Task(String status, Timestamp timestamp) {
		this.status = status;
		this.timestamp = timestamp;
	}

	public Task(com.legeyda.mts.model.Task task) {
		this.status = task.getStatus().toString();
		this.timestamp = task.getTimestamp();
	}

}
