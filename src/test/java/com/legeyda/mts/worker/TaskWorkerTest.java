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
		final AtomicReference<Instant> now = new AtomicReference<>(Instant.ofEpochMilli(0));

		final Store<UUID, Task> store = new MemoryStore<>();

		final TaskWorker testee = new TaskWorker();
		testee.setClock(now::get);
		testee.setTaskStore(store);
		testee.setSleepDurationMillis(1);
		executorService.submit(testee);

		final UUID id = UUID.randomUUID();
		store.write(id, (Optional<Task> old) -> Optional.of(new TaskImpl(Task.Status.created, now.get())));
		testee.accept(id);


		now.set(Instant.ofEpochMilli(5*60*1000-1));
		new Sleep(10).run();
		Optional<Task> task = store.read(id);
		assertThat(task).isPresent();
		assertThat(task.get().getStatus()).isEqualTo(Task.Status.running);


		now.set(Instant.ofEpochMilli(5*60*1000+1));
		new Sleep(10).run();
		task = store.read(id);
		assertThat(task).isPresent();
		assertThat(task.get().getStatus()).isEqualTo(Task.Status.finished);
		assertThat(task.get().getTimestamp()).isEqualTo(now.get());

	}



}
