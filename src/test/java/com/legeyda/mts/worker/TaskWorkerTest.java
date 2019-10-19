package com.legeyda.mts.worker;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.model.TaskImpl;
import com.legeyda.mts.store.MemoryStore;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicReference;
import static org.assertj.core.api.Assertions.*;

public class TaskWorkerTest {

	private ExecutorService executorService;

	@Before
	public void start() {
		executorService = Executors.newSingleThreadExecutor();
	}

	@After
	public void stop() {
		executorService.shutdown();
	}

	@Test
	public void test() {
		final AtomicReference<Instant> currentTime = new AtomicReference<>(Instant.ofEpochMilli(0));

		final Store<UUID, Task> store = new MemoryStore<>();

		final TaskWorker testee = new TaskWorker();
		testee.setClock(currentTime::get);
		testee.setTaskStore(store);
		testee.setSleepDurationMillis(1);
		executorService.submit(testee);

		// добавляем таск
		final UUID id = UUID.randomUUID();
		store.write(id, (Optional<Task> ignoredOldValue) -> Optional.of(new TaskImpl(Task.Status.CREATED, currentTime.get())));
		testee.accept(id);

		// через менее чем 2 минуты ещё выполняется
		currentTime.set(Instant.ofEpochMilli(2*60*1000-1));
		new Sleep(10).run();
		Optional<Task> task = store.read(id);
		assertThat(task).isPresent();
		assertThat(task.get().getStatus()).isEqualTo(Task.Status.RUNNING);

		// через более чем 2 минуты уже выполнилось и проставилось текущее время
		currentTime.set(Instant.ofEpochMilli(2*60*1000+1));
		new Sleep(10).run();
		task = store.read(id);
		assertThat(task).isPresent();
		assertThat(task.get().getStatus()).isEqualTo(Task.Status.FINISHED);
		assertThat(task.get().getTimestamp()).isEqualTo(currentTime.get());

	}



}
