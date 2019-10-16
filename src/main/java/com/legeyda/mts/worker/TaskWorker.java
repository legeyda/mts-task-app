package com.legeyda.mts.worker;

import com.legeyda.mts.gen.model.TaskStatus;
import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;

import java.util.Date;
import java.util.LinkedList;
import java.util.Optional;
import java.util.Queue;
import java.util.UUID;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.function.Consumer;

public class TaskWorker implements Consumer<UUID>, Runnable {

	private final Queue<UUID> inputQueue = new ConcurrentLinkedQueue<>();

	private Store<UUID, TaskStatus> taskStore;

	public void setTaskStore(Store<UUID, TaskStatus> taskStore) {
		this.taskStore = taskStore;
	}

	/** взять в работу задачу */
	@Override
	public void accept(UUID id) {
		taskStore.write(id, (Optional<Task> existingTask) -> {
			if(existingTask.isPresent() && Task.Status.created==existingTask.get().getStatus()) {
				inputQueue.offer(id);
				return Optional.of(new TaskStatus(Task.Status.running, new Date()));
			} else {
				return existingTask;
			}
		});
	}

	/** переводить задачи на завершённый статус*/
	@Override
	public void run() {
		while(true) {
			final Queue<UUID> outputQueue = new LinkedList<>();
			while(!inputQueue.isEmpty()) {
				final Optional<UUID> id = Optional.ofNullable(inputQueue.poll());
				if (id.isPresent()) {
					final Optional<Task> task = taskStore.read(id.get());
					if (task.isPresent() && Task.Status.running == task.get().getStatus()) {
						// если со статусами всё норм и время ожидания ещё не истекло, кладём обратно в очередь позже попробуем ещё
						if (System.currentTimeMillis() - task.get().getTimestamp().getTime() < 5*60*1000) {
							outputQueue.offer(id.get());
						}
					}
				}
			}
			inputQueue.addAll(outputQueue);
			new Sleep(1000).run();
		}
	}
}
