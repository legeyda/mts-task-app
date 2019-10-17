package com.legeyda.mts.worker;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class TaskWorker implements Consumer<UUID>, Runnable {

	private final Queue<UUID> inputQueue = new ConcurrentLinkedQueue<>();

	private Store<UUID, Task> taskStore;
	private Supplier<Instant> currentTime;
	private Integer sleepDurationMillis = 1000;

	@Autowired
	public void setTaskStore(Store<UUID, Task> taskStore) {
		this.taskStore = taskStore;
	}

	@Autowired
	public void setClock(Supplier<Instant> currentTime) {
		this.currentTime = currentTime;
	}

	public void setSleepDurationMillis(Integer sleepDurationMillis) {
		this.sleepDurationMillis = sleepDurationMillis;
	}

	/** взять задачу в работу */
	@Override
	public void accept(UUID id) {
		taskStore.write(id, (Optional<Task> existingTask) -> {
			if(existingTask.isPresent() && Task.Status.created==existingTask.get().getStatus()) {
				inputQueue.offer(id);
				return Optional.of(new TaskImpl(Task.Status.running, this.currentTime.get()));
			}
			return existingTask;
		});
	}

	/** переводить задачи на завершённый статус истечении 5 минут*/
	@Override
	public void run() {
		try {
			while (true) {
				final Queue<UUID> outputQueue = new LinkedList<>();
				while (!inputQueue.isEmpty()) {
					final Optional<UUID> id = Optional.ofNullable(inputQueue.poll());
					if (id.isPresent()) {
						final Optional<Task> task = taskStore.read(id.get());
						if (task.isPresent() && Task.Status.running == task.get().getStatus()) {
							if (task.get().getTimestamp().plusSeconds(5 * 60).isAfter(this.currentTime.get())) {
								// если время ожидания ещё не истекло, кладём обратно в очередь позже попробуем ещё
								outputQueue.offer(id.get());
							} else {
								this.taskStore.write(id.get(), (Optional<Task> dbValue) ->
										Optional.of(new TaskImpl(Task.Status.finished, this.currentTime.get())));
							}
						}
					}
				}
				inputQueue.addAll(outputQueue);
				new Sleep(this.sleepDurationMillis).run();
			}
		} finally {
			//
		}
	}
}
