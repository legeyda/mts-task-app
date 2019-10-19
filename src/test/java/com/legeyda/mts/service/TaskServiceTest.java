package com.legeyda.mts.service;

import com.legeyda.mts.model.Task;
import com.legeyda.mts.store.MemoryStore;
import com.legeyda.mts.store.Store;
import com.legeyda.mts.util.Sleep;
import com.legeyda.mts.worker.TaskWorker;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicReference;

import static org.assertj.core.api.Assertions.assertThat;

/** главный интеграционный тест */
public class TaskServiceTest {
	ExecutorService executorService;
	AtomicReference<Instant> currentTime;
	Store<UUID, Task> store;
	TaskWorker worker;
	TaskService service;

	@Before
	public void setUp() {
		currentTime = new AtomicReference<>(Instant.ofEpochSecond(0));
		store = new MemoryStore<>();

		worker = new TaskWorker();
		worker.setClock(currentTime::get);
		worker.setTaskStore(store);
		worker.setSleepDurationMillis(1);

		service = new TaskService();
		service.setTaskStore(store);
		service.setTaskWorker(worker);
		service.setSleepDuration(1);
		service.setCurrentTime(currentTime::get);

		executorService = Executors.newCachedThreadPool();
		executorService.submit(worker);
	}

	@After
	public void stop() {
		executorService.shutdown();
	}

	@Test
	public void test() throws ExecutionException, InterruptedException {

		// добавляем задачу
		final UUID id = service.createTask();
		new Sleep(10).run();

		// воркер сразу подхватил её и перевёл на running
		assertThat(service.getTaskSync(id)).isPresent();
		assertThat(store.read(id).get().getStatus()).isEqualTo(Task.Status.RUNNING);
		assertThat(store.read(id).get().getTimestamp()).isEqualTo(Instant.ofEpochSecond(0));

		// вызываем getFinishedTask
		Future<Optional<Task>> result = executorService.submit(() -> service.getFinishedTask(id));
		new Sleep(10).run();

		// сразу не отвечает
		assertThat(result).isNotDone();

		// через почти 2 минуты не отвечает
		currentTime.set(Instant.ofEpochSecond(2*60-1));
		new Sleep(10).run();
		assertThat(result).isNotDone();

		// если 2 минуты прошло, то отвечает статусом running так как воркер еще не обработал
		currentTime.set(Instant.ofEpochSecond(2*60 + 1));
		new Sleep(10).run();
		assertThat(result).isDone();

		// за минуту до завершения таска вызываем getFinishedTask
		currentTime.set(Instant.ofEpochSecond(4*60));
		result = executorService.submit(() -> service.getFinishedTask(id));

		// за мгновение до завершения таска не возвращает ответ, т-к 2 минуты не прошло, а таск ещё не готов
		currentTime.set(Instant.ofEpochSecond(5*60-1));
		new Sleep(10).run();
		assertThat(result).isNotDone();

		// сразу после завершения таска возвращает результат (в итоге через минуту), т-к таск уже выполнен
		currentTime.set(Instant.ofEpochSecond(5*60+1));
		new Sleep(10).run();
		assertThat(result).isDone();
		assertThat(result.get().get().getStatus()).isEqualTo(Task.Status.FINISHED);

		// если таск уже готов, возвращается сразу
		result = executorService.submit(() -> service.getFinishedTask(id));
		new Sleep(10).run();
		assertThat(result).isDone();
		assertThat(result.get().get().getStatus()).isEqualTo(Task.Status.FINISHED);

	}

}
