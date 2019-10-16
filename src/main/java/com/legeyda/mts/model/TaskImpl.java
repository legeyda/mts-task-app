package com.legeyda.mts.model;

import java.sql.Timestamp;
import java.util.Date;

public class TaskImpl implements Task {

	private Status status;
	private Date timestamp;

	public TaskImpl(Status status, Date timestamp) {
		this.status = status;
		this.timestamp = timestamp;
	}

	@Override
	public Status getStatus() {
		return null;
	}

	@Override
	public Date getTimestamp() {
		return null;
	}
}
