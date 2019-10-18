package com.legeyda.mts.service;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import com.legeyda.mts.worker.TaskWorker;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.task.AsyncTaskExecutor;
import org.springframework.stereotype.Component;
import org.springframework.web.bind.annotation.PathVariable;

import javax.annotation.PostConstruct;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Component
public class TaskService {


	private Store<UUID, Task> taskStore;
	private TaskWorker taskWorker;
	private AsyncTaskExecutor executor;

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

	public UUID createTask() {
		final UUID id = UUID.randomUUID();
		taskStore.write(id, (Optional<Task> task) ->
				Optional.of(new TaskImpl(Task.Status.created, Instant.now())));
		taskWorker.accept(id);
		return id;
	}

	public Optional<Task> getFinishedTask(@PathVariable("taskId") UUID taskId) {
		final long deadline = System.currentTimeMillis() + 2*60*1000;
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
		return result;
	}

	public Optional<Task> getTaskSync(@PathVariable("taskId") UUID id) {
		return taskStore.read(id);
	}

}
