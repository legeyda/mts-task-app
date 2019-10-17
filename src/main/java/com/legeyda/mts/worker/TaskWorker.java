package com.legeyda.mts.worker;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;
import java.util.function.Supplier;

@Component
public class TaskWorker implements Consumer<UUID>, Runnable {

	private final Queue<UUID> queue = new ConcurrentLinkedQueue<>();

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
	public void accept(final UUID id) {
		taskStore.write(id, (Optional<Task> existingTask) -> {
			if(existingTask.isPresent() && Task.Status.created==existingTask.get().getStatus()) {
				queue.offer(id);
				return Optional.of(new TaskImpl(Task.Status.running, this.currentTime.get()));
			}
			return existingTask;
		});
	}

	/** переводить задачи на завершённый статус истечении 5 минут*/
	@Override
	public void run() {
		while (true) {
			final List<UUID> tryAgainLater = new LinkedList<>();
			while (!queue.isEmpty()) {
				final UUID id = queue.poll();
				this.taskStore.write(id, (Optional<Task> oldValue) -> {
					if(oldValue.isPresent()
							&& Task.Status.running == oldValue.get().getStatus()) {
						if(oldValue.get().getTimestamp().plusSeconds(5 * 60).isAfter(this.currentTime.get())) {
							tryAgainLater.add(id);
						} else {
						    return Optional.of(new TaskImpl(Task.Status.finished, this.currentTime.get()));
                        }
					}
					return oldValue;
				});
			}
			queue.addAll(tryAgainLater);
			new Sleep(this.sleepDurationMillis).run();
		}
	}


}
