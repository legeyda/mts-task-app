package com.legeyda.mts.web;

import com.legeyda.mts.gen.api.TaskApiController;
import com.legeyda.mts.gen.model.TaskStatus;
import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import com.legeyda.mts.worker.TaskWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.context.request.NativeWebRequest;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import java.util.UUID;


@Controller
public class TaskApiControllerImpl extends TaskApiController {

	private Store<UUID, Task> taskStore;
	private TaskWorker taskWorker;
	private AsyncTaskExecutor executor;

	public TaskApiControllerImpl(NativeWebRequest request) {
		super(request);
	}

	@Autowired
	public void setTaskStore(Store<UUID, Task> taskStore) {
		this.taskStore = taskStore;
	}

	@Autowired
	public void setTaskWorker(TaskWorker taskWorker) {
		this.taskWorker = taskWorker;
	}

	@Autowired
	public void setExecutor(AsyncTaskExecutor executor) {
		this.executor = executor;
	}

	@PostConstruct
	public void init() {
		this.executor.execute(this.taskWorker);
	}

	public ResponseEntity<UUID> createTask() {
		final UUID id = UUID.randomUUID();
		taskStore.write(UUID.randomUUID(), (Optional<Task> task) ->
				Optional.of(new TaskImpl(Task.Status.created, Instant.now())));
		taskWorker.accept(id);
		return new ResponseEntity<>(id, HttpStatus.ACCEPTED); // todo тут лучше HttpStatus.CREATED
	}

	public ResponseEntity<TaskStatus> getFinishedTask(UUID taskId) {
		final long deadline = System.currentTimeMillis() + 5*60*1000;
		Optional<Task> result;
		while(true) {
			result = taskStore.read(taskId);
			if(!result.isPresent()
			   || Task.Status.finished.equals(result.get().getStatus())
			   || System.currentTimeMillis() > deadline) {
				break;
			}
			new Sleep(1000).run();
		}
		return result
				.map((Task task) -> new ResponseEntity<>(createRestApiTaskObject(task), HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	public ResponseEntity<TaskStatus> getTaskSync(UUID id) {
		return taskStore.read(id)
				.map((Task task) -> {
					return new ResponseEntity<>(createRestApiTaskObject(task), HttpStatus.OK);
				})
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));

	}

	private TaskStatus createRestApiTaskObject(Task task) {
		final TaskStatus result = new TaskStatus();
		result.setTimestamp(task.getTimestamp().atOffset(ZoneOffset.UTC));
		return result;
	}


}
