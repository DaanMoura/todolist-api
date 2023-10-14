package dev.danielmoura.todolist.task;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import dev.danielmoura.todolist.utils.Utils;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/tasks")
public class TaskController {
    
    @Autowired
    private ITaskRepository taskRepository;

    @PostMapping("/")
    public ResponseEntity<TaskModel> create(@RequestBody TaskModel taskModel, HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");
        taskModel.setIdUser(idUser);

        LocalDateTime currentDate = LocalDateTime.now();
        if (currentDate.isAfter(taskModel.getStartAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Start date must be in the future");
        }

        if (taskModel.getStartAt().isAfter(taskModel.getEndAt()) || taskModel.getStartAt().isEqual(taskModel.getEndAt())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "End date must be after start date");
        }

        TaskModel task = this.taskRepository.save(taskModel);
        return ResponseEntity.ok().body(task);
    }

    @GetMapping("/")
    public ResponseEntity<List<TaskModel>> getAll(HttpServletRequest request) {
        UUID idUser = (UUID) request.getAttribute("idUser");
        List<TaskModel> tasks = this.taskRepository.findByIdUser(idUser);
        return ResponseEntity.ok().body(tasks);
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskModel> update(@RequestBody TaskModel taskModel, @PathVariable UUID id, HttpServletRequest request) {
        TaskModel task = this.taskRepository.findById(id).orElse(null);

        if (task == null) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Task not found");
        }

        UUID idUser = (UUID) request.getAttribute("idUser");

        if (!task.getIdUser().equals(idUser)) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "User do not have permission to update this task");
        }

        Utils.copyNonNullProperties(taskModel, task);
        TaskModel updatedTask = this.taskRepository.save(task);

        return ResponseEntity.ok().body(updatedTask);
    }
}
