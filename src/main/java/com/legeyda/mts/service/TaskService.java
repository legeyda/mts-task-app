package com.legeyda.mts.service;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.CurrentTime;
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
import java.util.function.Supplier;

@Component
public class TaskService {

	private Supplier<Instant> currentTime = new CurrentTime();
	private Store<UUID, Task> taskStore;
	private TaskWorker taskWorker;
	private AsyncTaskExecutor executor;
	private Integer sleepDuration = 1000;

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

	@Autowired
	public void setCurrentTime(Supplier<Instant> currentTime) {
		this.currentTime = currentTime;
	}

	public void setSleepDuration(Integer sleepDuration) {
		this.sleepDuration = sleepDuration;
	}

	@PostConstruct
	public void init() {
		this.executor.execute(this.taskWorker);
	}

	public UUID createTask() {
		final UUID id = UUID.randomUUID();
		taskStore.write(id, (Optional<Task> task) ->
				Optional.of(new TaskImpl(Task.Status.CREATED, Instant.now())));
		taskWorker.accept(id);
		return id;
	}

	public Optional<Task> getFinishedTask(@PathVariable("taskId") UUID taskId) {
		final Instant deadline = this.currentTime.get().plusSeconds(2*60);
		Optional<Task> result;
		while(true) {
			result = taskStore.read(taskId);
			if(!result.isPresent()
			   || Task.Status.FINISHED.equals(result.get().getStatus())
			   || this.currentTime.get().isAfter(deadline)) {
				break;
			}
			new Sleep(this.sleepDuration).run();
		}
		return result;
	}

	public Optional<Task> getTaskSync(@PathVariable("taskId") UUID id) {
		return taskStore.read(id);
	}

}
