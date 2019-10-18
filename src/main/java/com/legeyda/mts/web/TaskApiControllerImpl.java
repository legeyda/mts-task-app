package com.legeyda.mts.web;

import com.legeyda.mts.gen.api.TaskApiController;
import com.legeyda.mts.gen.model.TaskStatus;
import com.legeyda.mts.model.Task;
import com.legeyda.mts.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.context.request.NativeWebRequest;

import java.time.ZoneOffset;
import java.util.UUID;


@Controller
public class TaskApiControllerImpl extends TaskApiController {

	private TaskService taskService;

	public TaskApiControllerImpl(NativeWebRequest request) {
		super(request);
	}

	@Autowired
	public void setTaskService(TaskService taskService) {
		this.taskService = taskService;
	}

	@Override
	public ResponseEntity<UUID> createTask() {
		return new ResponseEntity<>(this.taskService.createTask(), HttpStatus.ACCEPTED); // todo тут лучше HttpStatus.CREATED
	}

	@Override
	public ResponseEntity<TaskStatus> getFinishedTask(@PathVariable("taskId") UUID taskId) {
		return taskService.getFinishedTask(taskId)
				.map((Task task) -> new ResponseEntity<>(createRestApiTaskObject(task), HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	@Override
	public ResponseEntity<TaskStatus> getTaskSync(@PathVariable("taskId") UUID id) {
		return taskService.getTaskSync(id)
				.map((Task task) -> new ResponseEntity<>(createRestApiTaskObject(task), HttpStatus.OK))
				.orElse(new ResponseEntity<>(HttpStatus.NOT_FOUND));
	}

	private TaskStatus createRestApiTaskObject(Task task) {
		final TaskStatus result = new TaskStatus();
		result.setStatus(task.getStatus().getGenStatus());
		result.setTimestamp(task.getTimestamp().atOffset(ZoneOffset.UTC));
		return result;
	}

}
