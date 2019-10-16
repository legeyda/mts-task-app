package com.legeyda.mts.web;

import com.legeyda.mts.gen.api.TaskApiController;
import com.legeyda.mts.gen.model.TaskStatus;
import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.TaskStore;
import com.legeyda.mts.util.Sleep;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.context.request.NativeWebRequest;

import java.util.Date;
import java.util.Optional;
import java.util.UUID;

public class TaskApiControllerImpl extends TaskApiController {


	public TaskApiControllerImpl(NativeWebRequest request) {
		super(request);
	}

	private TaskStore taskStore;

	public void setTaskStore(TaskStore taskStore) {
		this.taskStore = taskStore;
	}

	@Override
	public ResponseEntity<UUID> createTask() {
		final UUID id = UUID.randomUUID();
		taskStore.write(UUID.randomUUID(), (Optional<Task> task) ->
				Optional.of(new TaskImpl(Task.Status.created, new Date())));
		return new ResponseEntity<>(id, HttpStatus.ACCEPTED); // todo тут лучше HttpStatus.CREATED
	}

	@Override
	public ResponseEntity<TaskStatus> getFinishedTask(UUID taskId) {



		final long deadline = System.currentTimeMillis() + 5*60*1000;
		Optional<Task> result;
		while(true) {
			result = taskStore.read(id);
			if(!result.isPresent()
			   || Task.Status.finished.equals(result.get().getStatus())
			   || System.currentTimeMillis() > deadline) {
				break;
			}
			new Sleep(1000).run();
		}
		return result;



	}

	@Override
	public ResponseEntity<TaskStatus> getTaskSync(UUID taskId) {
		final UUID id;
		try {
			id = UUID.fromString(idStr);
		} catch (IllegalArgumentException e) {
			return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
		}

		final Optional<Task> result = taskStore.read(id);
		return result.map((Task task) -> {
			new ResponseEntity<>(task, HttpStatus.OK);
		});

	}
}
