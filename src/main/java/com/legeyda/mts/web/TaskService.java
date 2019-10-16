package com.legeyda.mts.web;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.TaskStore;
import com.legeyda.mts.util.Sleep;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class TaskService {

	private TaskStore taskStore;

	public ResponseEntity<UUID> createTask() {

	}

	ResponseEntity<Task> getTaskSync(final String idStr) {



	}

	Optional<Task> getFinishedTask(final UUID id) {

	}


}
